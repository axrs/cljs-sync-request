(defproject io.axrs/cljs-sync-request "1.0.0"
  :description "A ClojureScript wrapper around sync-request. Make synchronous HTTP requests."
  :min-lein-version "2.8.1"
  :dependencies [[thheller/shadow-cljs "2.8.37" :scope "provided"]
                 [org.clojure/clojurescript "1.10.520" :scope "provided"]]
  :clean-targets ["target"]
  :source-paths ["src"]
  :test-paths ["test"]
  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                    :username      :env/clojars_username
                                    :password      :env/clojars_password
                                    :sign-releases false}]])
