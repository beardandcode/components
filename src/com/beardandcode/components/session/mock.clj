(ns com.beardandcode.components.session.mock
  (:import [java.util UUID])
  (:require [ring.middleware.session.store :refer [SessionStore]]))

(defprotocol IMockSessionStore
  (clear-sessions [_])
  (list-sessions [_]))

(deftype MockSessionStore [session-map]
  SessionStore
  (read-session [_ key]
    (@session-map key))
  (write-session [_ key data]
    (let [key (or key (str (UUID/randomUUID)))]
      (swap! session-map assoc key data)
      key))
  (delete-session [_ key]
    (swap! session-map dissoc key)
    nil)

  IMockSessionStore
  (clear-sessions [_] (reset! session-map {}))
  (list-sessions [_] (into [] @session-map)))

(defn new-mock-session-store []
  (MockSessionStore. (atom {})))
