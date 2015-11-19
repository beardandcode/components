(ns com.beardandcode.components.error-logger
  (:require [com.stuartsierra.component :as component]
            [raven-clj.ring :refer [wrap-sentry]]))


(defprotocol IErrorLogger
  (wrap-handler [this handler options]))

(defn wrap-error-logger [handler logger options]
  (if (satisfies? IErrorLogger logger)
    (wrap-handler logger handler options)))

(defrecord SentryErrorLogger [dsn]
  IErrorLogger
  (wrap-handler [this handler options]
    (if (nil? dsn)
      (fn [req] (handler req))
      (wrap-sentry handler dsn {:extra options
                                :namespaces ["com.beardandcode"]}))))

(defn new-sentry-logger [dsn]
  (map->SentryErrorLogger {:dsn dsn}))
