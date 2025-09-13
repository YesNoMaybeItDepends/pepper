(ns pepper.strat-test
  (:require [clojure.test :as test :refer [deftest is testing]]
            [clojure.test.check.clojure-test :as test.check]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))

;; impl

(defn total-supply [supply] supply) ;; scvs and army
(defn army-supply [supply] supply) ;; exclude scvs
(defn worker-supply [supply] supply) ;; only scvs

(defn unit-ratio [unit-count total-supply]
  (if (and (pos? unit-count)
           (pos? total-supply))
    (/ unit-count total-supply)
    0))

(defn under-ratio? [ratio target-ratio]
  (< ratio target-ratio))

(defn over-ratio? [ratio target-ratio]
  (> ratio target-ratio))

;; test

(def strat
  {:build {:barracks 4
           :academy 1
           :engineering-bay 1}
   :train {:marine 10/12
           :medic 1/12
           :firebat 1/12}
   :research [:stim-packs]
   :upgrade {:u-238-shells 1
             :terran-infantry-weapons 1
             :terran-infantry-armor 1}})

(def gen-supply {:scvs gen/nat
                 :marines gen/nat
                 :firebats gen/nat
                 :medics gen/nat})

(def gen-strat (gen/return strat))

(deftest test-under-ratio?
  (is (true? (let [medics 1
                   army-supply 13
                   target-ratio 1/12
                   ratio (unit-ratio medics army-supply)]
               (and (under-ratio? ratio target-ratio)
                    (not (over-ratio? ratio target-ratio)))))))

(deftest test-over-ratio?
  (is (true? (let [medics 2
                   army-supply 11
                   target-ratio 1/12
                   ratio (unit-ratio medics army-supply)]
               (and (over-ratio? ratio target-ratio)
                    (not (under-ratio? ratio target-ratio)))))))