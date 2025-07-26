(ns pepper.utils.profiling-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.test.alpha :as st]
            [clojure.spec.alpha :as s]
            [pepper.profiling :as p]))

(st/instrument)
(s/check-asserts true)

(deftest test-profiling
  (testing "profiler"
    (let [target 1000
          margin 50
          start (p/now-ns)
          _sleep (Thread/sleep target)
          end (p/now-ns)
          duration (p/times-ns->duration-ms [start end])]
      (is (true? (p/within-margin-min-max duration target margin))))))
