(ns pepper.interop.core-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.spec.test.alpha :as st]
   [clojure.spec.alpha :as s]
   [pepper.interop.core :as i]))

(st/instrument)
(s/check-asserts true)

(def data {:hello-world {:routes {:hello {:handler (fn [request] {:hello :world})}}
                         :request {:call/id [:hello]
                                   :call/input []}
                         :response {:hello :world}}

           :request-and-response-fns {:args {:request {:valid [[[:hello] []]]
                                                       :invalid [[]
                                                                 [nil]
                                                                 [nil nil]
                                                                 [:hello nil]
                                                                 [nil []]
                                                                 [[:hello] nil]]}
                                             :response {:valid [[{:call/id [:hello :world]
                                                                  :call/input []}
                                                                 :success
                                                                 "some data of any kind"]]
                                                        :invalid [[nil
                                                                   :success
                                                                   "some data of any kind"]
                                                                  [{:call/id [:hello :world]
                                                                    :call/input []}
                                                                   nil
                                                                   "some data of any kind"]
                                                                  [{:call/id [:hello :world]
                                                                    :call/input []}
                                                                   :error
                                                                   nil]]}}}})

(deftest interop-test

  (testing "request and response fns"
    (let [data (get-in data [:request-and-response-fns])]
      (testing "invalid request args"
        (is (false? (every? (partial s/valid? :request/args) (get-in data [:args :request :invalid])))))

      (testing "valid request args"
        (is (every? (partial s/valid? :request/args) (get-in data [:args :request :valid]))))

      (testing "invalid response args"
        (is (false? (every? (partial s/valid? :response/args) (get-in data [:args :response :invalid])))))

      (testing "valid response args"
        (is (every? (partial s/valid? :response/args) (get-in data [:args :response :valid]))))))

  (testing "api"
    (testing "hello world"
      (let [{:keys [routes
                    request
                    response]} (get-in data [:hello-world])
            router (i/router routes)
            api (i/api-handler router)]

        (testing "handles route"
          (is (= response (api request))))))))