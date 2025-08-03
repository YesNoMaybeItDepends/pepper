(ns pepper.game.job-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.spec.test.alpha :as st]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sg]
            [pepper.game.unit :as unit]
            [pepper.game.jobs :as jobs]
            [pepper.game.macro :as macro]
            [pepper.mocking :as mocking]))

(testing "We can get a list of unit jobs"
  (is (= (jobs/get-unit-jobs {:unit-jobs {1 {:unit-id 1}}})
         [{:unit-id 1}])))

(testing "We can delete a unit job"
  (is (= (jobs/delete-job {:unit-jobs {1 {:unit-id 1}
                                       2 {:unit-id 2}}} {:unit-id 1})
         {:unit-jobs {2 {:unit-id 2}}})))

(testing "We can filter pending jobs"
  (is (= (jobs/filter-pending-jobs [{:unit-id 1 :run? false}
                                    {:unit-id 2 :run? true}])
         [{:unit-id 1 :run? false}])))

(testing "We can execute a job"
  (let [dummy-action (fn [game job] (get-in game [:unit-id (:unit-id job) :hello]))]
    (is (= ((jobs/execute-job! {:unit-id {1 {:hello :world}}})
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
               (jobs/process-jobs! {})
               (jobs/process-jobs! {})
               (jobs/process-jobs! {}))))))