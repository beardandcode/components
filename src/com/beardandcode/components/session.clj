(ns com.beardandcode.components.session
  (:require [ring.middleware.session.memory :refer [memory-store]]))

(defn new-memory-session-store []
  (memory-store))
