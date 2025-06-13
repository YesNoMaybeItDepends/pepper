(ns pepper.htn.impl.compound-test
  (:require
   [clojure.test :refer :all]
   [pepper.htn.impl.compound :as c]))

(deftest test-task
  (testing "A compound task"
    (testing "has a name"
      (is (not (nil? (:name (c/task {:name "hello"}))))))

    (testing "has a set (vector) of methods"
      (is (vector? (:methods (c/task {:name "hello"}))))
      (is (vector? (:methods (c/task {:name "hello" :methods []}))))
      (is (vector? (:methods (c/task {:name "hello" :methods 1})))))))

(deftest test-methods
  (testing "A method"

    (testing "has a name"
      (is (some? (:name (c/method {}))))
      (is (some? (:name (c/method {:name nil}))))
      (is (some? (:name (c/method {:name "hello"})))))

    (testing "has a set (vector) of preconditions"
      (is (some? (:preconditions (c/method {}))))
      (is (some? (:preconditions (c/method {:preconditions nil}))))
      (is (some? (:preconditions (c/method {:preconditions []})))))

    (testing "every precondition is a function"
      (is (every? fn? (:preconditions (c/method {}))))
      (is (every? fn? (:preconditions (c/method {:preconditions "hello"}))))
      (is (every? fn? (:preconditions (c/method {:preconditions [(fn [hello] hello)]}))))
      (is (not (every? fn? (:preconditions (c/method {:preconditions ["hello"]}))))))

    (testing "has a set (vector) of tasks"
      (is (some? (:tasks (c/method {}))))
      (is (some? (:tasks (c/method {:tasks nil}))))
      (is (some? (:tasks (c/method {:tasks []})))))

    (testing "every task is a valid primitive task or a compound task"
      (is (= true false) "TODO: handle this"))))

(deftest test-validating-methods
  (let [invalid-method? (fn [result] (not (nil? result)))]

    (is (invalid-method? (c/validate-method nil)))
    (is (invalid-method? (c/validate-method {})))
    (is (invalid-method? (c/validate-method {:name :test})))
    (is (invalid-method? (c/validate-method {:name :test
                                             :tasks []
                                             :preconditions nil})))
    (is (invalid-method? (c/validate-method {:name :test
                                             :tasks []
                                             :preconditions []})))
    (is (invalid-method? (c/validate-method {:name :test
                                             :tasks []
                                             :preconditions [(str "hello" "world")]})))
    (is (not (invalid-method? (c/validate-method {:name :test
                                                  :tasks []
                                                  :preconditions [(fn [] (str "hello" "world"))]}))))))