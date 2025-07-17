(ns pepper.core-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.spec.test.alpha :as st]
   [pepper.mocking :as mocking])
  (:import (bwapi Game Unit UnitType)))

(st/instrument)

(def data {:unit-opts [[#(.getID %) (int 3)]
                       [#(.isFlying %) true]
                       [#(.getType %) bwapi.UnitType/Terran_SCV]]})

(deftest jbwapi-test
  (testing "Mocking jbwapi with Mockito"
    (testing "Can mock a SCV"
      (is (= bwapi.UnitType/Terran_SCV (.getType (mocking/mock bwapi.Unit (get-in data [:unit-opts]))))))

    (testing "Can see all the fields of a bwapi unit"
      (is (seq? (seq (-> (mocking/mock bwapi.Unit (get-in data [:unit-opts]))
                         (bean)
                         (keys))))))

    (testing "Can mock a game"
      (is (= bwapi.Game (type (mocking/mock bwapi.Game [])))))

    (testing "Can mock a game with a list of units"
      (let [units (list (mocking/mock bwapi.Unit []))
            game (mocking/mock bwapi.Game [[#(.getAllUnits %) units]])]
        (is (= 1 (count (.getAllUnits game))))))))