# cljs-sync-request

A ClojureScript wrapper around the NPM [sync-request](https://github.com/ForbesLindesay/sync-request) module

> Note: `cljs-sync-request` does not bundle with `sync-request`

## Latest Version

[![Clojars Project](https://img.shields.io/clojars/v/io.axrs/cljs-sync-request.svg)](https://clojars.org/io.axrs/cljs-sync-request)
[![cljdoc badge](https://cljdoc.org/badge/io.axrs/cljs-sync-request)](https://cljdoc.org/d/io.axrs/cljs-sync-request/CURRENT)

## Why?

Sometimes you just need to perform a synchronous HTTP requests without worrying about entering callback hell or propagating
`core.async` `go` blocks and `channels` throughout your code. I use `cljs-sync-request` primarily in AWS Lambda functions
where I don't need to do any asynchronous work and am typically wait for a result before continuing processing.

**As the base `sync-request` library suggests, you are unlikely to want to use this library in production.**

## Example Usage

```clojure
(ns io.axrs.cljs-sync-request.example
  (:require
    [io.axrs.cljs.sync-request.core :refer [json-post]]))

(defn check-status []
  (let [{:keys [status body headers] :as response} (json-post "https://some.url/here" {:id "1234"})]
    (if (= 200 status)
      (continue-processing body)
      (handle-other-codes response))))
```

