(ns pepper.game.unit-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.test.alpha :as st]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sg]
            [clojure.reflect :as reflect]
            [clojure.repl :refer :all]
            [clojure.java.data :as jd]
            #_[pepper.core :as core]
            [pepper.game.unit :as unit]
            [pepper.mocking :as mocking])
  (:import (bwapi Unit)))

(st/instrument)

(deftest unit-test
  (let [mock-unit-opts [[#(.getID %) (int 3)]
                        [#(.isFlying %) true]
                        [#(.getType %) bwapi.UnitType/Terran_SCV]]
        #_mock-unit #_(fn [opts] (mocking/mock Unit opts))
        mock-unit (mocking/mock Unit mock-unit-opts)]
    (testing "Can datafy a unit from a bwapi unit"
      (is (map? (unit/datafy mock-unit))))

    (testing "A unit has an ID"
      (is (= 3 (-> (unit/datafy mock-unit)
                   (unit/id)))))))

