(defproject io.axrs/cljs-sync-request "0.0.1-SNAPSHOT"
  :description "A ClojureScript wrapper around sync-request. Make synchronous HTTP requests."
  :min-lein-version "2.8.1"
  :dependencies [[thheller/shadow-cljs "2.8.37" :scope "provided"]
                 [org.clojure/clojurescript "1.10.520" :scope "provided"]]
  :clean-targets ["target"]
  :source-paths ["src"]
  :test-paths ["test"])
