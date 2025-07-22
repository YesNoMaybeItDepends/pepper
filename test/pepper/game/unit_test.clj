(ns pepper.game.unit-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.test.alpha :as st]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sg]
            [pepper.game.unit :as unit]
            [pepper.mocking :as mocking]))

(st/instrument)

;; not sure if it makes sense to test unit yet
;; since right now it works on both maps and objects
;; but do I really want that?
;; im also not sure how to test for something that isnt initialized all at once
(deftest unit-test
  (testing "can get id")
  (testing "can get exists?")
  (testing "can get worker?")
  (testing "can get player"))