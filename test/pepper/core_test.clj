(ns pepper.core-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.spec.test.alpha :as st]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sg]
   [clojure.reflect :as reflect]
   [clojure.repl :refer :all]
   [clojure.java.data :as jd])
  #_(:import (bwapi Game Text)))

(st/instrument)

;;;; 

(defn when-then [x w t]
  (-> (w x)
      (org.mockito.Mockito/when)
      (.thenReturn t)
      (.getMock)))

(defn mock [class opts]
  (reduce (fn [acc [m v]]
            (when-then acc m v))
          (org.mockito.Mockito/mock class)
          opts))

(def data {:unit-opts [[#(.getID %) (int 3)]
                       [#(.isFlying %) true]
                       [#(.getType %) bwapi.UnitType/Terran_SCV]]})

(deftest jbwapi-test
  (testing "Mocking jbwapi with Mockito"
    (testing "Can mock a SCV"
      (is (= bwapi.UnitType/Terran_SCV (.getType (mock bwapi.Unit (get-in data [:unit-opts]))))))

    (testing "Can see all the fields of a bwapi unit"
      (is (seq? (seq (-> (mock bwapi.Unit (get-in data [:unit-opts]))
                         (bean)
                         (keys))))))

    (testing "Can mock a game"
      (is (= bwapi.Game (type (mock bwapi.Game [])))))

    (testing "Can mock a game with a list of units"
      (let [units (list (mock bwapi.Unit []))
            game (mock bwapi.Game [[#(.getAllUnits %) units]])]
        (is (= 1 (count (.getAllUnits game))))))))