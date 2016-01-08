(defproject bowling-kata "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.omcljs/om "1.0.0-alpha29-SNAPSHOT"]
                 [prismatic/schema "1.0.4"]
                 [midje "1.6.0" :exclusions [org.clojure/clojure]]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [figwheel-sidecar "0.5.0-SNAPSHOT" :scope "test"]]

  :clean-targets ^{:protect false} ["resources/public/js" "target"]
  )