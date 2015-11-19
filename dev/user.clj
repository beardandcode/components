(ns user
  (:require [clojure.repl :refer :all]
            [clojure.test :refer [run-all-tests]]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [vinyasa.reimport :refer [reimport]]
            [vinyasa.pull :refer [pull]]))

(defn refresh-and [f]
  (refresh :after (symbol "user" f)))

(defn test-all [] (run-all-tests #"^com.beardandcode.components.*-test$"))
(defn test-unit [] (run-all-tests #"^com.beardandcode.components.(?!integration).*-test$"))
(defn test-integration [] (run-all-tests #"^com.beardandcode.components.integration.*-test$"))
