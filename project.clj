(defproject kaan-sein-tool "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clojure-csv/clojure-csv "2.0.1"]
                 [clj-pdf "2.2.22"]]
  :main ^:skip-aot kaan-sein-tool.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
