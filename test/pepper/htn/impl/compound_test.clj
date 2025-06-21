(ns pepper.htn.impl.compound-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as st]
   [pepper.htn.impl.compound :as c]
   [pepper.htn.impl.primitive :as p]))

(st/instrument)
(s/check-asserts true)

(deftest test-task
  (testing "Compound tasks"
    (let [;; primitive
          valid-primitive {:task/name :some-primitive
                           :task/preconditions []
                           :task/effects [inc]
                           :task/operator inc}
          invalid-primitive {:task/name :some-primitive
                             :task/preconditions []
                             :task/effects [inc]
                             :task/operator nil}
          ;; method
          valid-method {:task/name :valid-method
                        :task/preconditions []
                        :task/subtasks [valid-primitive]}
          invalid-method {:task/name :valid-method
                          :task/preconditions []
                          :task/subtasks [invalid-primitive]}
          ;; compound
          valid-compound {:task/name :valid-compound
                          :task/methods [valid-method]}
          invalid-compound {:task/name :valid-compound
                            :task/methods [invalid-method]}]
      (is (some? (c/task valid-compound)))
      (is (thrown? Exception (c/task invalid-compound))))))