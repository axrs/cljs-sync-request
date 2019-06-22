(ns io.axrs.cljs-sync-request.core-test
  (:require
    ["sync-request" :as sr]
    [clojure.test :refer [deftest testing is]]
    [clojure.walk :as walk]
    [io.axrs.cljs-sync-request.core :as core]))

(defn- rand-str
  ([] (rand-str 24))
  ([len]
   (apply str (take len (repeatedly #(char (+ (rand 26) 65)))))))

(defn- stringify-keys [m]
  (let [f (fn [[k v]] (if (keyword? k) [(name k) v] [(str k) v]))]
    (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(def random-uuid-str (comp str random-uuid))

(deftest httpdump-io-test
  (let [request (core/wrap-sync-request sr)
        url "https://posthere.io/dbe9-4f42-8f97"
        expected-body {:id (random-uuid-str)}]

    (testing "deleting to url"
      (request {:method core/DELETE
                :url    url}))

    (testing "posting data"
      (let [{:keys [status headers]} (request {:url          url
                                               :method       core/POST
                                               :content-type core/json
                                               :body         expected-body})]
        (is (= 200 status))
        (is (map? headers))))

    (testing "getting the data just posted"
      (let [{:keys [status body headers]} (request {:url    url
                                                    :method core/GET})]
        (is (= 200 status))
        (is (map? headers))
        (is (vector? body))
        (is (pos? (count body)))
        (is (some (fn [posted]
                    (= (stringify-keys expected-body)
                       (core/json->clj (get posted "body"))))
              body))))))

(comment (deftest github-test
           (let [request (core/wrap-sync-request sr)
                 url "https://api.github.com/user"]
             (request {:url     url
                       :headers {"Authorization" "token"
                                 "User-Agent"    "sync-request"}
                       :method  core/GET}))))

