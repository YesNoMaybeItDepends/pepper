(ns pepper.game.query-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.spec.test.alpha :as st]
   [clojure.spec.alpha :as s]
   [pepper.game.query :as query]))

(st/instrument)
(s/check-asserts true)


(def query {:query/id [:game :get-all-units]})
#_(def query {:query/kind :query/game-units}) ;; or this

(def units {:game/units [{:unit/id 1
                          :unit/type :unit.type/scv}]})

(deftest some-test

  (testing "Sends request"
    (is (true? (-> (query/send-request {} 4 query)
                   (query/loading? query)))))

  (testing "Received succesful response"
    (is (false? (-> (query/send-request {} 4 query)
                    (query/receive-response
                     5 query
                     {:succes? true
                      :result units})
                    (query/loading? query)))))

  (testing "Succesful response is available"
    (is (false? (-> (query/send-request {} 4 query)
                    (query/receive-response
                     5 query
                     {:succes? true
                      :result units})
                    (query/available? query)))))

  (testing "Gets available data"
    (is (= (-> (query/send-request {} 4 query)
               (query/receive-response
                4 query
                {:success? true
                 :result units})
               (query/get-result query))
           units))))


