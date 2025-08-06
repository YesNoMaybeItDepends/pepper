(ns pepper.game.gathering-test
  {:clj-kondo/config '{:lint-as {clojure.test.check.clojure-test/defspec clj-kondo.lint-as/def-catch-all}}}
  (:require
   [clojure.spec.test.alpha :as st]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.properties :as prop]
   [pepper.game.jobs :as jobs]
   [pepper.game.gathering :as gathering]
   [pepper.game.production :as production]
   [pepper.game.resources :as resources]
   [pepper.generators :as gen])
  (:import
   (bwapi UnitType)))

(deftest macro-test
  (let [state {:units-by-id {1 {:id 1
                                :type :mineral-field
                                :idle? true}
                             2 {:id 2
                                :type :scv
                                :idle? false}
                             3 {:id 3
                                :type :scv
                                :idle? true}
                             4 {:id 4
                                :type :bro
                                :idle? false}
                             5 {:id 5
                                :type UnitType/Resource_Mineral_Field
                                :idle? false}
                             6 {:id 6
                                :type UnitType/Resource_Mineral_Field_Type_2
                                :idle? false}
                             7 {:id 7
                                :type UnitType/Resource_Mineral_Field_Type_3
                                :idle? false}}}]
    (testing "We can find our workers"
      (is (= (gathering/get-workers state)
             [{:id 2 :type :scv :idle? false} {:id 3 :type :scv :idle? true}])))

    (testing "We can find our idle workers"
      (is (= (gathering/get-idle-workers state)
             [{:id 3 :type :scv :idle? true}])))

    (testing "We can assign a unit a new mining job"
      (is (= (jobs/assign-unit-job {} (gathering/mining-job [1 2]))
             {:unit-jobs {1 {:job :mining
                             :action gathering/go-mine!
                             :unit-id 1
                             :mineral-field-id 2}}})))

    (testing "We can assign a random mineral to a worker"
      (is (= ((gathering/assign-random-mineral [2]) 1)
             [1 2])))

    (testing "We can assign random minerals to a sequence of workers"
      (is (= (map (gathering/assign-random-mineral [2 2]) [1 1])
             [[1 2]
              [1 2]])))

    (testing "We can get mineral-fields"
      (is (= (gathering/get-mineral-fields {:units-by-id {1 {:id 1
                                                             :type :mineral-field}
                                                          2 {:id 2
                                                             :type :scv}}})
             [{:id 1 :type :mineral-field}])))

    (testing "We can process idle workers in a macro step"
      (let [state-1 {:units-by-id {1 {:id 1
                                      :type :scv
                                      :idle? true}
                                   2 {:id 2
                                      :type :mineral-field
                                      :idle? true}}}
            state-2 {:units-by-id {1 {:id 1
                                      :type :scv
                                      :idle? true}
                                   2 {:id 2
                                      :type :mineral-field
                                      :idle? true}}
                     :unit-jobs {1 {:job :mining
                                    :action gathering/go-mine!
                                    :unit-id 1
                                    :mineral-field-id 2}}}]
        (is (= (gathering/process-idle-workers state-1)
               state-2))))))