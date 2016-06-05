(defproject com.beardandcode/components "0.1.3"
  :description "A set of components for building software for use with com.stuartsierra/component"
  :url "http://bearandcode.com"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}

  :min-lein-version "2.0.0"
  
  :plugins [[lein-ancient "0.6.7"]
            [jonase/eastwood "0.2.1"]
            [lein-bikeshed "0.2.0"]
            [lein-kibit "0.1.2"]]
  
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [com.stuartsierra/component "0.3.1"]
                 [raven-clj "1.3.1"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [org.postgresql/postgresql "9.3-1102-jdbc4"]
                 [com.mchange/c3p0 "0.9.5"]
                 [yesql "0.5.1"]
                 [javax.servlet/servlet-api "2.5"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [clj-http "2.0.0"]
                 [metrics-clojure-ring "2.5.1"]]

  :aliases {"checkall" ["do" ["check"] ["kibit"] ["eastwood"] ["bikeshed"]]}

  :deploy-repositories [["clojars" {:sign-releases false}]]

  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.10"]
                                  [leiningen #=(leiningen.core.main/leiningen-version)]
                                  [im.chit/vinyasa "0.3.4"]
                                  [reloaded.repl "0.2.1"]]
                   :source-paths ["dev"]}})
