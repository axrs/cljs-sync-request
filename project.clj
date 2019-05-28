(defproject io.axrs/cljs-sync-request "0.0.1-SNAPSHOT"
  :description "A ClojureScript wrapper around sync-request. Make synchronous web requests with cross-platform support."
  :min-lein-version "2.8.1"
  :dependencies [[thheller/shadow-cljs "2.8.37"]
                 [org.clojure/clojurescript "1.10.520"]]
  :clean-targets ["target"]
  :source-paths ["src"]
  :test-paths ["test"])
