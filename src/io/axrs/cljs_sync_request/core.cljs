(ns io.axrs.cljs-sync-request.core
  (:require
    ["sync-request" :as sync-request]))

(defonce ^:private ^:const DELETE "DELETE")
(defonce ^:private ^:const GET "GET")
(defonce ^:private ^:const HEAD "HEAD")
(defonce ^:private ^:const POST "POST")
(defonce ^:private ^:const PUT "PUT")

(defn clj->json [e] (js/JSON.stringify (clj->js e)))
(defn json->clj [s] (js->clj (js/JSON.parse s)))

(def ^:private js-opts {:decode json->clj
                        :encode clj->json})
(def ^:private json-headers {"Content-Type" "application/json"
                             "Accept"       "application/json"})

(defn- request [method url {:keys [body] :as context} {:keys [encode decode inflate deflate]
                                                       :or   {inflate identity deflate identity}}]
  (let [context (if body (update context :body encode) context)
        context (-> context inflate clj->js)
        response (sync-request method (str url) context)]
    (deflate
      {:status  (.-statusCode response)
       :body    (decode (.getBody response "utf8"))
       :headers (clj->js (.-headers response))})))

(defn json-head
  ([url] (json-head url js-opts))
  ([url {:as opts}]
   (request HEAD url {:headers json-headers} opts)))

(defn json-delete
  ([url] (json-delete url js-opts))
  ([url {:as opts}]
   (request DELETE url {:headers json-headers} opts)))

(defn json-get
  ([url] (json-get url js-opts))
  ([url {:as opts}]
   (request GET url {:headers json-headers} opts)))

(defn json-put
  ([url body] (json-put url body js-opts))
  ([url body {:as opts}]
   (request PUT url {:headers json-headers :body body} opts)))

(defn json-post
  ([url body] (json-post url body js-opts))
  ([url body {:as opts}]
   (request POST url {:headers json-headers :body body} opts)))
