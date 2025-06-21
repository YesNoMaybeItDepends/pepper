(ns pepper.htn.impl.primitive-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.spec.test.alpha :as st]
   [clojure.spec.alpha :as s]
   [pepper.htn.impl.primitive :as p]))

(st/instrument)
(s/check-asserts true)

(deftest test-task
  (testing "Primitive tasks"
    (let [valid-primitive {:task/name :some-primitive
                           :task/preconditions []
                           :task/effects [inc]
                           :task/operator inc}
          invalid-primitive {:task/name :some-primitive
                             :task/preconditions []
                             :task/effects [inc]
                             :task/operator nil}]
      (is (some? (p/task valid-primitive)))
      (is (thrown? Exception (p/task invalid-primitive)))))

  (testing "preconditions can be keywords"
    (is (some? (p/task {:task/name :some-primitive
                        :task/preconditions [:something]
                        :task/effects []
                        :task/operator identity}))))
  (testing "preconditions can be anonymous"
    (is (some? (p/task {:task/name :some-primitive
                        :task/preconditions [#(:something %)]
                        :task/effects []
                        :task/operator identity}))))
  (testing "effects can be anonymous"
    (is (some? (p/task {:task/name :some-primitive
                        :task/preconditions []
                        :task/effects [#(assoc % :something true)]
                        :task/operator identity}))))
  (testing "operators can be anonymous"
    (is (some? (p/task {:task/name :some-primitive
                        :task/preconditions []
                        :task/effects []
                        :task/operator #(assoc % :something true)})))))