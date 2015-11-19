(ns com.beardandcode.components.database-test
  (:require [clojure.test :refer :all]
            [com.beardandcode.components.database :as db]))

(deftest normalise-url
  (testing "Heroku style urls"
    (is (= (db/normalise-url "postgres://user:pass@host:1234/some-db")
           "jdbc:postgresql://host:1234/some-db?user=user&password=pass")))
  (testing "Should leave jdbc urls alone"
    (is (= (db/normalise-url "jdbc:postgresql://host:1234/some-db?user=user&password=pass")
           "jdbc:postgresql://host:1234/some-db?user=user&password=pass"))))
