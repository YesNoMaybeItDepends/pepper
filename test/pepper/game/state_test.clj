(ns pepper.game.state-test
  #_(:require [clojure.test :refer [deftest is testing]]
              [clojure.spec.test.alpha :as st]
              [pepper.game.state :as state]))

;; (st/instrument)

;; (deftest state-test
;;   (testing "When we initialize the state"

;;     (testing "we know our player id"
;;       (is (= 1 (:self-id (state/init-state {:self {:id 1}
;;                                             :frame 0})))))

;;     (testing "we know the players in the game, indexed by id"
;;       (is (= {:frame 0
;;               :self-id 1
;;               :players-by-id {1 {:id 1}}}
;;              (state/init-state {:frame 0
;;                                 :self {:id 1}
;;                                 :players [{:id 1}]}))))

;;     (testing "we know that the frame is 0"
;;       (is (= 0 (state/get-frame (state/init-state {:frame 0}))))

;;       (testing "and we throw when it is not 0"
;;         (is (thrown? Exception (state/init-state {:frame 1})))))))