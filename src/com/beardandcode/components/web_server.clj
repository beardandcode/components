(ns com.beardandcode.components.web-server
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [ring.adapter.jetty :refer [run-jetty]]
            [com.beardandcode.components.routes :refer [build-routes]]))

(defprotocol IWebServer
  (port [this] "Get the port of the webserver"))

(defn- real-port [jetty-server]
  (-> jetty-server .getConnectors first .getLocalPort))

(defrecord WebServer [ip-address port jetty routes]
  
  component/Lifecycle
  (start [web]
    (if jetty
      web
      (let [jetty-server (run-jetty (build-routes routes)
                                    {:ip ip-address
                                     :port port
                                     :join? false})]
        (log/infof "Started web server on http://%s:%s"
                   ip-address (real-port jetty-server))
        (assoc web :jetty jetty-server))))

  (stop [web]
    (if (not jetty)
      web
      (let [port (real-port jetty)]
        (do (.stop jetty)
            (log/infof "Stopped web server on http://%s:%s"
                       ip-address port)
            (assoc web :jetty nil)))))

  IWebServer
  (port [web]
    (real-port jetty)))

(defn new-web-server [ip-address port]
  (map->WebServer {:ip-address ip-address
                   :port port}))
