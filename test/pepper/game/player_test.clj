(ns pepper.game.player-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.test.alpha :as st]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sg]
            [pepper.game.player :as players]
            [pepper.mocking :as mocking]))

(st/instrument)

(deftest player-test
  (testing "We can get a player's"

    (testing "id"
      (is (= 1 (players/id {:id 1}))))

    (testing "minerals"
      (is (= 50 (players/minerals
                 {:minerals 50}))))

    (testing "gas"
      (is (= 50 (players/gas
                 {:gas 50}))))

    (testing "supply total"
      (is (= 2 (players/supply-total {:supply-total 2}))))

    (testing "supply used"
      (is (= 2 (players/supply-used {:supply-used 2}))))))