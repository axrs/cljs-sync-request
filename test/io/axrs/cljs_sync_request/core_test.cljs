(ns io.axrs.cljs-sync-request.core-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [clojure.walk :as walk]
    [io.axrs.cljs-sync-request.core :as core]
    ["sync-request" :as sr]))

(defn- rand-str
  ([] (rand-str 24))
  ([len]
   (apply str (take len (repeatedly #(char (+ (rand 26) 65)))))))

(defn- stringify-keys [m]
  (let [f (fn [[k v]] (if (keyword? k) [(name k) v] [(str k) v]))]
    (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(def random-uuid-str (comp str random-uuid))

(defn- mock-response [status headers body]
  #js {:statusCode status
       :headers    headers
       :getBody    (constantly body)})

(deftest inflate-test

  (testing "Provides an opportunity to extend or transform the context before making the request"
    (let [authorization (str "Bearer " (random-uuid-str))
          url "https://axrs.dev/inflate/test"
          path [:headers "Authorization"]
          inflate (fn [context] (assoc-in context path authorization))
          response-id (random-uuid-str)]
      (binding [core/*sync-request* (fn [method actual-url js-request]
                                      (is (= core/POST method))
                                      (is (= url actual-url))
                                      (is (object? js-request))
                                      (is (= authorization (.. js-request -headers -Authorization)))
                                      (mock-response 200 #js {} (core/clj->json {:id response-id})))]
        (let [{:keys [status headers body] :as response} (core/request core/POST url {:headers core/json-headers} {:inflate inflate})]
          (is (map? response))
          (is (= 200 status))
          (is (= {} (js->clj headers)))
          (is (= {"id" response-id} (core/json->clj body))))))))

(deftest deflate-test

  (testing "Provides an opportunity to extend or transform the response"
    (let [url "https://axrs.dev/deflate/test"
          deflate (fn [response] (assoc response :deflated? true))
          response-id (random-uuid-str)]
      (binding [core/*sync-request* (fn [method actual-url _]
                                      (is (= core/PUT method))
                                      (is (= url actual-url))
                                      (mock-response 301 #js {} (core/clj->json {:id response-id})))]
        (let [{:keys [status headers body deflated?] :as response} (core/request core/PUT url {} {:deflate deflate})]
          (is (map? response))
          (is (= 301 status))
          (is (= {} (js->clj headers)))
          (is deflated?)
          (is (= {"id" response-id} (core/json->clj body))))))))

(deftest encode-test

  (testing "A function used to encode the request :body if present"
    (let [url "https://axrs.dev/encode/test"
          encode (fn [body] (-> body (assoc :encoded? true) core/clj->json))
          response-id (random-uuid-str)]
      (binding [core/*sync-request* (fn [method actual-url js-request]
                                      (is (= core/POST method))
                                      (is (= url actual-url))
                                      (mock-response 201 #js {} (.. js-request -body)))]
        (let [{:keys [status body]} (core/request core/POST url {:body {:id response-id}} {:encode encode})]
          (is (= 201 status))
          (is (= {"id"       response-id
                  "encoded?" true}
                 (core/json->clj body))))))))

(deftest decode-test

  (testing "A function used to decode the result :body if present"
    (let [url "https://axrs.dev/encode/test"
          decode (fn [body]
                   (-> body core/json->clj walk/keywordize-keys))
          response-id (random-uuid-str)]
      (binding [core/*sync-request* (fn [method actual-url _]
                                      (is (= core/POST method))
                                      (is (= url actual-url))
                                      (mock-response 202 #js {} (core/clj->json {:id response-id})))]
        (let [{:keys [status body]} (core/request core/POST url {} {:decode decode})]
          (is (= 202 status))
          (is (= {:id response-id} body)))))))

(defn assert-basic-json-request [expected-method expected-url & [body]]
  (fn [actual-method actual-url actual-context actual-opts]
    (is (= expected-method actual-method))
    (is (= expected-url actual-url))
    (is (= core/json-headers (:headers actual-context)))
    (is (= body (:body actual-context)))
    (is (= core/js-opts actual-opts))))

(deftest json-head

  (testing "performs a JSON head request with default json-opts"
    (let [url "https://axrs.dev/json-head"]
      (with-redefs [core/request (assert-basic-json-request core/HEAD url)]
        (core/json-head url)))))

(deftest json-delete

  (testing "performs a JSON delete request with default json-opts"
    (let [url "https://axrs.dev/json-delete"]
      (with-redefs [core/request (assert-basic-json-request core/DELETE url)]
        (core/json-delete url)))))

(deftest json-get

  (testing "performs a JSON get request with default json-opts"
    (let [url "https://axrs.dev/json-get"]
      (with-redefs [core/request (assert-basic-json-request core/GET url)]
        (core/json-get url)))))

(deftest json-put

  (testing "performs a JSON put request with default json-opts"
    (let [url "https://axrs.dev/json-put"
          body {:id (rand-str)}]
      (with-redefs [core/request (assert-basic-json-request core/PUT url body)]
        (core/json-put url body)))))

(deftest json-post

  (testing "performs a JSON post request with default json-opts"
    (let [url "https://axrs.dev/json-post"
          body {:id (rand-str)}]
      (with-redefs [core/request (assert-basic-json-request core/POST url body)]
        (core/json-post url body)))))

(deftest httpdump-io-test
  (core/set-sync-request! sr)

  (let [url "https://posthere.io/dbe9-4f42-8f97"
        expected-body {:id (random-uuid-str)}]

    (testing "deleting to url"
      (core/json-delete url))

    (testing "posting data"
      (let [{:keys [status headers]} (core/json-post url expected-body)]
        (is (= 200 status))
        (is (map? headers))))

    (testing "getting the data just posted"
      (let [{:keys [status body headers]} (core/json-get url)]
        (is (= 200 status))
        (is (map? headers))
        (is (vector? body))
        (is (pos? (count body)))
        (is (some (fn [posted]
                    (= (stringify-keys expected-body)
                       (core/json->clj (get posted "body"))))
              body))))))
