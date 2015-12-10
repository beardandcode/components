(ns com.beardandcode.components.email.mock
  (:require [com.beardandcode.components.email :as email]))

(defprotocol IMockEmail
  (list-emails [_])
  (clear-emails [_]))

(defrecord MockEmail [emails]
  IEmail
  (send-email [_ to subject message]
    (swap! emails conj {:to to :subject subject :message message})
    {})

  IMockEmail
  (list-emails [_] @emails)
  (clear-emails [_] (reset! emails [])))

(defn new-mock-email-service []
  (MockEmail. (atom [])))
