(ns com.beardandcode.components.database
  (:import [com.mchange.v2.c3p0 ComboPooledDataSource]
           [org.postgresql.util PGobject]
           [org.apache.http.client.utils URIBuilder URLEncodedUtils]
           [java.net URI])
  (:require [com.stuartsierra.component :as component]
            [clojure.string :refer [split]]
            [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [yesql.util :refer [slurp-from-classpath create-root-var]]
            [yesql.generate :refer [generate-query-fn]]
            [yesql.queryfile-parser :refer [parse-tagged-queries]]
            [metrics.timers :refer [timer time!]]
            [com.beardandcode.components.healthcheck :as healthcheck]))

(defn- query-params [uri]
  (reduce #(assoc %1 (.getName %2) (.getValue %2))
          {} (URLEncodedUtils/parse uri "utf8")))

(defn- to-jdbc-url [uri]
  (let [creds (if-let [user-info (.getUserInfo uri)]
                (split user-info #":")
                (let [params (query-params uri)]
                  [(params "user") (params "password")]))
        host (.getHost uri)
        port (.getPort uri)
        path (.getPath uri)]
    (str "jdbc:postgresql://" host ":" port path "?user=" (first creds) "&password=" (last creds))))

(defn strip-database [jdbc-url]
  (let [url (.substring jdbc-url 5)
        uri (URIBuilder. url)]
    (.setPath uri "/")
    (to-jdbc-url (.build uri))))

(defn normalise-url [url]
  (if (= (subs url 0 5) "jdbc:")
    url
    (to-jdbc-url (URI. url))))

(defn pool [spec]
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass "org.postgresql.Driver")
               (.setJdbcUrl (:connection-uri spec))
               ;; expire excess connections after 30
               ;; minutes of inactivity:
               (.setMaxIdleTimeExcessConnections (* 30 60))
               ;; expire connections after 3 hours of
               ;; inactivity:
               (.setMaxIdleTime (* 3 60 60)))]
    {:datasource cpds}))

(extend-protocol jdbc/IResultSetReadColumn
  PGobject
  (result-set-read-column [pgobj metadata idx]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "citext" value
        :else value))))

(defprotocol IDatabase
  (conn [db]))


(defrecord Database [spec conn]
  component/Lifecycle

  (start [database]
    (if conn
      database
      (assoc database :conn (pool spec))))

  (stop [database]
    (if (not conn)
      database
      (do (.close (-> database :conn :datasource))
          (assoc database :conn nil))))

  healthcheck/IHealthcheck
  (alive? [_]
    (try
      (let [results (jdbc/query conn ["SELECT 1 AS out"])]
        (and (= 1 (count results))
             (= 1 (-> results first :out))))
      (catch org.postgresql.util.PSQLException ex
        (log/error ex "Failed to process db alive?")
        false)
      (catch java.sql.SQLException ex
        (log/error ex "Failed to process db alive?")
        false)))

  IDatabase
  (conn [_] conn))

(defn new-database [url]
    (map->Database {:spec {:connection-uri (normalise-url url)}}))

(defn defqueries
    "Wrap the underlying yesql query function so that we generate something
   that we can pass IDatabase rather than {:connection conn}. This uses the same
   functions as defqueries/generate-var in yesql.

   A generated query function will also create a timer to record the duration of
   all executions of the query and report it using clojure-metrics."
  [filename]
  (let [timer-identifier (nth (re-matches #"^.*?([^/]+)\.sql$" filename) 1)]
    (doall (->> filename
                slurp-from-classpath
                parse-tagged-queries
                (map #(let [yesql-fn (generate-query-fn % {})
                            query-timer (timer ["sql" timer-identifier (:name %)])]
                        (create-root-var
                         (:name %)
                         (fn [database args]
                            (let [conn (if (satisfies? IDatabase database)
                                         (conn database) database)]
                              (time! query-timer
                                     (yesql-fn args {:connection conn})))))))))))

(defmacro with-transaction [binding & body]
  `(jdbc/db-transaction* (conn ~(second binding))
                         (^{:once true} fn* [~(first binding)] ~@body)
                         ~@ (rest (rest binding))))
