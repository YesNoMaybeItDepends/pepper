(ns pepper.htn.impl.compound-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [pepper.htn.impl.compound :as t]))

(deftest test-task
  (testing "A compound task"
    (testing "has a name"
      (is (not (nil? (:name (t/task {:name "hello"}))))))

    (testing "has a set (vector) of methods"
      (is (vector? (:methods (t/task {:name "hello"}))))
      (is (vector? (:methods (t/task {:name "hello" :methods []}))))
      (is (vector? (:methods (t/task {:name "hello" :methods 1})))))))

(deftest test-methods
  (testing "A method"

    (testing "has a name"
      (is (some? (:name (t/method {}))))
      (is (some? (:name (t/method {:name nil}))))
      (is (some? (:name (t/method {:name "hello"})))))

    (testing "has a set (vector) of preconditions"
      (is (some? (:preconditions (t/method {}))))
      (is (some? (:preconditions (t/method {:preconditions nil}))))
      (is (some? (:preconditions (t/method {:preconditions []})))))

    (testing "every precondition is a function"
      (is (every? fn? (:preconditions (t/method {}))))
      (is (every? fn? (:preconditions (t/method {:preconditions "hello"}))))
      (is (every? fn? (:preconditions (t/method {:preconditions [(fn [hello] hello)]}))))
      (is (not (every? fn? (:preconditions (t/method {:preconditions ["hello"]}))))))

    (testing "has a set (vector) of tasks"
      (is (some? (:tasks (t/method {}))))
      (is (some? (:tasks (t/method {:tasks nil}))))
      (is (some? (:tasks (t/method {:tasks []})))))

    (testing "every task is a valid primitive task or a compound task"
      (is (= true false) "TODO: handle this"))))

(deftest test-validating-methods
  (let [invalid-method? (fn [result] (not (nil? result)))]

    (is (invalid-method? (t/validate-method nil)))
    (is (invalid-method? (t/validate-method {})))
    (is (invalid-method? (t/validate-method {:name :test})))
    (is (invalid-method? (t/validate-method {:name :test
                                             :tasks []
                                             :preconditions nil})))
    (is (invalid-method? (t/validate-method {:name :test
                                             :tasks []
                                             :preconditions []})))
    (is (invalid-method? (t/validate-method {:name :test
                                             :tasks []
                                             :preconditions [(str "hello" "world")]})))
    (is (not (invalid-method? (t/validate-method {:name :test
                                                  :tasks []
                                                  :preconditions [(fn [] (str "hello" "world"))]}))))))