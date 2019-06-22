(ns io.axrs.cljs-sync-request.core
  (:require
    [cljs.reader :as reader]
    [clojure.string :as string]))

(def json "JSON MIME Type" "application/json")
(def edn "EDN MIME Type" "application/edn")

(defn clj->json
  "A no assumption clj->JSON encoding function used by all `json-*` request methods with a `:body`. Can be replaced by
  specifying `:encode` in the `opts` of any request method."
  [e] (js/JSON.stringify (clj->js e)))

(defn json->clj
  "A no assumption JSON->clj decoding function used by all `json-*` request methods decode a response `:body`. Can be
  replaced by specifying the `:decode` in the `opts` of any request method."
  [s] (js->clj (js/JSON.parse s)))

(def ^:private mime-transformers
  {edn  {:encode pr-str
         :decode reader/read-string}
   json {:encode clj->json
         :decode json->clj}})

(def DELETE "`method` used to perform a `DELETE` request" "DELETE")
(def GET "`method` used to perform a `GET` request" "GET")
(def HEAD "`method` used to perform a `HEAD` request" "HEAD")
(def POST "`method` used to perform a `POST` request" "POST")
(def PUT "`method` used to perform a `PUT` request" "PUT")

(defn- encode [{:as transformers} {:keys [body content-type] :as request}]
  (if (and body content-type)
    (if-let [encoder (get-in transformers [content-type :encode])]
      (update request :body encoder)
      request)
    request))

(defn- body [^js response]
  (.getBody response "utf-8"))

(defn- decode [{:as transformers} {:keys [method] :as request} ^js response]
  (let [status (.-statusCode response)
        headers (js->clj (.-headers response))
        content-type (string/lower-case (get headers "content-type" ""))
        result {:status status :headers headers}
        decoder (first (keep (fn [[t fns]]
                               (when (string/starts-with? content-type t)
                                 (:decode fns)))
                         transformers))
        decode (comp (or decoder identity) body)]
    (if (and (not= HEAD method)
             (not= 204 status))
      (assoc result :body (decode response))
      result)))

(defn wrap-sync-request
  "Wraps `sync-request`, returning a single arity function to perform http requests.
   Transformers are provided to encode request bodies and response bodies.
   JSON is encoding by `io.axrs.cljs-sync-request.core/clj->json`, and decoded by `io.axrs.cljs-sync-request.core/clj->json`.
   EDN is encoding by `pr-str`, and decoded by `clojure.reader/read-string`.

   Refer to [sync-request options](https://github.com/ForbesLindesay/sync-request/) for all available features of the
   request map including `:timeout`, `:retry`, `:maxRetries`, `:qs`, and more.

   ```
   (def transformers {\"application/json\" {:decode #(js->clj (js/JSON.parse %)) :encode #(js/JSON.stringify (clj->js %))}})

   (def request (wrap-sync-request sync-request transformers))

   (request {:body {:id \"123\"} :content-type \"application/json\" :method \"POST\"})
    ```"
  [sync-request & [transformers]]
  (let [transformers (or transformers mime-transformers)
        encode (partial encode transformers)
        decode (partial decode transformers)]
    (fn request [{:keys [method url content-type] :as request}]
      (let [request (update request :headers (partial merge {"accept" (or content-type json)}))]
        (->> (encode request)
             (clj->js)
             (sync-request method url)
             (decode request))))))
