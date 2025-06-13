(ns pepper.htn.impl.primitive-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [pepper.htn.impl.primitive :as t]))



(deftest test-task
  (testing "Testing that a primitive task"

    (testing "has a name"
      (is (not (nil? (:name (t/task {})))))
      (is (not (nil? (:name (t/task {:name "hello"}))))))

    (testing "has a set (vector) of preconditions"
      (is (vector? (:preconditions (t/task {}))))
      (is (vector? (:preconditions (t/task {:preconditions nil}))))
      (is (vector? (:preconditions (t/task {:preconditions []}))))
      (is (vector? (:preconditions (t/task {:preconditions [1]})))))

    (testing "has a set (vector) of effects"
      (is (vector? (:effects (t/task {}))))
      (is (vector? (:effects (t/task {:effects nil}))))
      (is (vector? (:effects (t/task {:effects []}))))
      (is (vector? (:effects (t/task {:effects [1]})))))

    (testing "has an operator function"
      (is (fn? (:operator (t/task {}))))
      (is (fn? (:operator (t/task {:operator 1}))))
      (is (fn? (:operator (t/task {:operator (fn [x] x)})))))))

