# cljs-sync-request

A ClojureScript wrapper around the NPM [sync-request](https://github.com/ForbesLindesay/sync-request) module

> Note: `cljs-sync-request` does not bundle with `sync-request`

## Latest Version

[![Clojars Project](https://img.shields.io/clojars/v/io.axrs/cljs-sync-request.svg)](https://clojars.org/io.axrs/cljs-sync-request)
[![cljdoc badge](https://cljdoc.org/badge/io.axrs/cljs-sync-request)](https://cljdoc.org/d/io.axrs/cljs-sync-request)
[![CircleCI](https://circleci.com/gh/axrs/cljs-sync-request.svg?style=svg)](https://circleci.com/gh/axrs/cljs-sync-request)

## Why?

Sometimes you just need to perform a synchronous HTTP requests without worrying about entering callback hell or propagating
`core.async` `go` blocks and `channels` throughout your code. I use `cljs-sync-request` primarily in AWS Lambda functions
where I don't need to do any asynchronous work and am typically wait for a result before continuing processing.

**As the base `sync-request` library suggests, you are unlikely to want to use this library in production.**

## Example Usage

```clojure
(ns io.axrs.cljs-sync-request.example
  (:require
    [io.axrs.cljs.sync-request.core :refer [wrap-sync-request]]
    ["sync-request" :as sync-request]])) ; When using shadow-cljs
    
(def transformers {"application/json" {:decode #(js->clj (js/JSON.parse %)) 
                                       :encode #(js/JSON.stringify (clj->js %))}})
(def request (wrap-sync-request sync-request transformers))

(defn check-status []
  (let [{:keys [status body headers] :as response} (request {:body {:id "123"} 
                                                             :content-type "application/json" 
                                                             :url "http://localhost"
                                                             :method "POST"})
    (if (= 200 status)
      (continue-processing body)
      (handle-other-codes response))))
```

