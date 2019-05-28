(ns io.axrs.cljs-sync-request.core-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [clojure.walk :as walk]
    [io.axrs.cljs-sync-request.core :as request]))

(def ^:private url "https://httpdump.io/q4awo")

(defn- rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn- stringify-keys [m]
  (let [f (fn [[k v]] (if (keyword? k) [(name k) v] [(str k) v]))]
    (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(deftest json-post-test

  (testing "Posts data and returns a response"
    (let [data {:someValues           (rand-str 20)
                "should-be-converted" 2
                :other-values         (rand-str 20)
                2                     {3 [4 5 6]}}
          {:keys [status body headers] :as actual} (request/json-post url data)]
      (is (map? actual))
      (is (= 200 status))

      (testing "posted value was a json string"
        (let [raw-body (get body "raw_post")]
          (is (= (stringify-keys data)
                 (request/json->clj raw-body)))))

      (testing "request and response body can be passed through encoding functions"
        (let [{:keys [status body headers] :as actual} (request/json-post url data {:encode (fn [body]
                                                                                              (-> body
                                                                                                  (assoc :encoded true)
                                                                                                  request/clj->json))
                                                                                    :decode (fn [body]
                                                                                              (js->clj
                                                                                                (js/JSON.parse body)
                                                                                                :keywordize-keys true))})]
          (is (map? actual))
          (is (= 200 status))

          (testing "posted value was a json string"
            (let [raw-body (:raw_post body)]
              (is (= (stringify-keys (assoc data :encoded true))
                     (request/json->clj raw-body))))))))))

(deftest json-get-test
  (cljs.pprint/pprint (request/json-get url)))

(deftest json-put-test)

(deftest json-delete-test)

(deftest json-head-test)

(clojure.test/run-tests 'io.axrs.cljs-sync-request.core-test)
