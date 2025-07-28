(ns pepper.game.unit-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.test.alpha :as st]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sg]
            [pepper.game.unit :as unit]
            [pepper.mocking :as mocking]))

(st/instrument)

(deftest unit-test
  (testing "We can get a unit by id"
    (is (= (-> {:units-by-id {1 {:id 1}}}
               (unit/get-unit 1))
           {:id 1})))

  (testing "We can know if a unit is ours"
    (is (true? (unit/ours? {:self-id 2}
                           {:player-id 2})))
    (is (false? (unit/ours? {:self-id 2}
                            {:player-id 1})))))