(ns com.beardandcode.components.email
  (:require [com.stuartsierra.component :as component]
            [clj-http.client :as client]))


(defprotocol IEmail
  (send-email [this to subject message]))

(defrecord Mailgun [base-url key from]
  IEmail
  (send-email [this to subject message]
    (let [response (client/post (str base-url "/messages")
                                {:basic-auth ["api" key]
                                 :form-params {:from from
                                               :to to
                                               :subject subject
                                               :text (:text message)
                                               :html (:html message)}})]
      response)))

(defn new-mailgun [base-url key from]
  (map->Mailgun {:base-url base-url
                 :key key
                 :from from}))
