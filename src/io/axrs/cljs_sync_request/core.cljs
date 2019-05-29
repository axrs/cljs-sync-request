(ns io.axrs.cljs-sync-request.core
  (:require
    ["sync-request" :as js-sync-request]))

(def ^:dynamic sync-request
  "Dynamic reference to the underlying Javascript [`sync-request`](https://github.com/ForbesLindesay/sync-request) module.

  Note: `sync-request` is not bundled with `cljs-sync-request`. Tested against version 6.1.0"
  js-sync-request)

(def DELETE "`method` used to perform a `DELETE` request" "DELETE")
(def GET "`method` used to perform a `GET` request" "GET")
(def HEAD "`method` used to perform a `HEAD` request" "HEAD")
(def POST "`method` used to perform a `POST` request" "POST")
(def PUT "`method` used to perform a `PUT` request" "PUT")

(defn clj->json
  "A no assumption clj->JSON encoding function used by all `json-*` request methods with a `:body`. Can be replaced by
  specifying `:encode` in the `opts` of any request method."
  [e] (js/JSON.stringify (clj->js e)))

(defn json->clj
  "A no assumption JSON->clj decoding function used by all `json-*` request methods decode a response `:body`. Can be
  replaced by specifying the `:decode` in the `opts` of any request method."
  [s] (js->clj (js/JSON.parse s)))

(defn- decode-body [decode response]
  (try
    (some-> (.getBody response "utf8") decode)
    (catch :default _)))

(defn request
  "Performs a `sync-request` with an `encode`d body and `inflate`d context. The `{:keys [status body headers] :as response}`
  is then returned `deflate`d and with a `decode`d body. Refer to `sync-request#Options` for all available features of
  the context map including :timeout, :retry, :maxRetries, :qs, and more.

  opts map
    :encode - A function that takes the body from the context and transforms it before performing the request. Defaults to `clj->json` for JSON requests
    :decode - A function that takes the body of the response and decodes it before returning a result. Defaults to `json->clj` for JSON requests
    :inflate - A function that takes the full context and transforms it before performing the request.
    :deflate - A function that takes the full response and transforms it before returning a result.

  (request
    POST
    {:body {:id \"123\"} :headers {\"Accept\" \"application/json\"}}
    {:encode (fn [request-body] (assoc request-body :token \"456\"))
     :decode (fn [response-body] (-> response-body json->clj (assoc :response-time (js/Date.))))
     :inflate (fn [request] (assoc-in request [:headers \"Authorization\"] (str \"Bearer 890\")))
     :deflate (fn [response] (dissoc response :headers))})
    "
  [method url {:keys [body] :as context} {:keys [encode decode inflate deflate]
                                          :or   {encode  identity
                                                 decode  identity
                                                 inflate identity
                                                 deflate identity}
                                          :as   opts}]
  (let [context (if body (update context :body encode) context)
        context (-> context inflate clj->js)
        response (sync-request method (str url) context)]
    (deflate
      {:status  (.-statusCode response)
       :body    (decode-body decode response)
       :headers (js->clj (.-headers response))})))

(def ^:private js-opts {:decode json->clj :encode clj->json})
(def ^:private json-headers {"Content-Type" "application/json"
                             "Accept"       "application/json"})

(defn json-head
  "Performs a synchronous JSON HEAD request to the specified `url`"
  ([url] (json-head url js-opts))
  ([url {:as opts}]
   (request HEAD url {:headers json-headers} (merge js-opts opts))))

(defn json-delete
  "Performs a synchronous JSON DELETE request to the specified `url`"
  ([url] (json-delete url js-opts))
  ([url {:as opts}]
   (request DELETE url {:headers json-headers} (merge js-opts opts))))

(defn json-get
  "Performs a synchronous JSON GET request to the specified `url`"
  ([url] (json-get url js-opts))
  ([url {:as opts}]
   (request GET url {:headers json-headers} (merge js-opts opts))))

(defn json-put
  "Performs a synchronous JSON PUT request to the specified `url` with a given edn `body` transformed into JSON"
  ([url body] (json-put url body js-opts))
  ([url body {:as opts}]
   (request PUT url {:headers json-headers :body body} (merge js-opts opts))))

(defn json-post
  "Performs a synchronous JSON POST request to the specified `url` with a given edn `body` transformed into JSON"
  ([url body] (json-post url body js-opts))
  ([url body {:as opts}]
   (request POST url {:headers json-headers :body body} (merge js-opts opts))))