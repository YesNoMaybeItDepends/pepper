(ns pepper.game.unit-test
  (:require
   [clojure.spec.test.alpha :as st]
   [clojure.test :refer [deftest is testing]]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.properties :as prop]
   [pepper.generators :as gens]
   [pepper.game.unit :as unit]
   [pepper.game.unit-type :as unit-type])
  (:import
   [bwapi UnitType]))

(st/instrument)

(deftest unit-test
  (testing "We can get a unit by id"
    (is (= (-> {:units-by-id {1 {:id 1}}}
               (unit/get-unit-by-id 1))
           {:id 1})))

  (testing "We can know if a unit is ours"
    (is (true? (unit/ours? {:self-id 2}
                           {:player-id 2})))
    (is (false? (unit/ours? {:self-id 2}
                            {:player-id 1}))))

  (testing "We can know if a unit is idle"
    (is (true? (:idle? {:idle? true}))))

  (testing "We can know if a unit is a mineral field"
    (is (true? (->> [{:type :mineral-field}]
                    (every? #(unit/type? % unit-type/mineral-field)))))))
