(ns com.beardandcode.components.healthcheck)

(defprotocol IHealthcheck
  (alive? [_]))
