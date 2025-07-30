(ns pepper.game.macro-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.spec.test.alpha :as st]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sg]
            [pepper.game.unit :as unit]
            [pepper.game.macro :as macro]
            [pepper.mocking :as mocking])
  (:import (bwapi UnitType)))

(st/instrument)

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
      (is (= (macro/get-workers state)
             [{:id 2 :type :scv :idle? false} {:id 3 :type :scv :idle? true}])))

    (testing "We can find our idle workers"
      (is (= (macro/get-idle-workers state)
             [{:id 3 :type :scv :idle? true}])))

    (testing "We can assign a unit a new mining job"
      (is (= (macro/assign-unit-job {} (macro/mining-job [1 2]))
             {:unit-jobs {1 {:job :mining
                             :action macro/go-mine!
                             :unit-id 1
                             :mineral-field-id 2}}})))

    (testing "We can assign a random mineral to a worker"
      (is (= ((macro/assign-random-mineral [2]) 1)
             [1 2])))

    (testing "We can assign random minerals to a sequence of workers"
      (is (= (map (macro/assign-random-mineral [2 2]) [1 1])
             [[1 2]
              [1 2]])))

    (testing "We can get mineral-fields"
      (is (= (macro/get-minerals {:units-by-id {1 {:id 1
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
                                    :action macro/go-mine!
                                    :unit-id 1
                                    :mineral-field-id 2}}}]
        (is (= (macro/process-idle-workers state-1)
               state-2))))

    (testing "We can get a list of unit jobs"
      (is (= (macro/get-unit-jobs {:unit-jobs {1 {:unit-id 1}}})
             [{:unit-id 1}])))

    (testing "We can delete a unit job"
      (is (= (macro/delete-job {:unit-jobs {1 {:unit-id 1}
                                            2 {:unit-id 2}}} {:unit-id 1})
             {:unit-jobs {2 {:unit-id 2}}})))

    (testing "We can filter pending jobs"
      (is (= (macro/filter-pending-jobs [{:unit-id 1 :run? false}
                                         {:unit-id 2 :run? true}])
             [{:unit-id 1 :run? false}])))

    (testing "We can execute a job"
      (let [dummy-action (fn [game job] (get-in game [:unit-id (:unit-id job) :hello]))]
        (is (= ((macro/execute-job! {:unit-id {1 {:hello :world}}})
                {:unit-id 1
                 :action dummy-action
                 :run? false})
               {:unit-id 1
                :action dummy-action
                :result :world
                :run? true}))))

    (testing "We can process jobs, __and for now, only once__"
      (let [dummy-action (fn [game job]
                           ((fnil inc 0) (:result job)))
            state {:unit-jobs {1 {:unit-id 1
                                  :action dummy-action
                                  :run? false}}}]
        (is (= {:unit-jobs {1 {:unit-id 1
                               :action dummy-action
                               :result 1
                               :run? true}}}
               (-> state
                   (macro/process-jobs {})
                   (macro/process-jobs {})
                   (macro/process-jobs {}))))))))