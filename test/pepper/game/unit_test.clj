(ns pepper.game.unit-test
  (:require
   [clojure.spec.test.alpha :as st]
   [clojure.test :refer [deftest is testing]]
   [pepper.game.unit :as unit])
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
    (is (true? (->> [{:type :mineral-field}
                     {:type UnitType/Resource_Mineral_Field}
                     {:type UnitType/Resource_Mineral_Field_Type_2}
                     {:type UnitType/Resource_Mineral_Field_Type_3}]
                    (every? unit/mineral-field?))))))
