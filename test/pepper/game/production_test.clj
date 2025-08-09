(ns pepper.game.production-test
  {:clj-kondo/config '{:lint-as {clojure.test.check.clojure-test/defspec clj-kondo.lint-as/def-catch-all}}}
  (:require
   [clojure.spec.test.alpha :as st]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.properties :as prop]
   [pepper.game.jobs :as jobs]
   [pepper.game.macro :as macro]
   [pepper.game.production :as production]
   [pepper.game.resources :as resources]
   [pepper.generators :as gen])
  (:import
   (bwapi UnitType)))


(defspec can-afford-everything-when-rich
  (prop/for-all [state gen/state
                 unit-type gen/unit-type]
                (let [state-rich (update state :resources (fn [{minerals :minerals
                                                                gas :gas
                                                                [used total] :supply}]
                                                            {:minerals (+ minerals 1500)
                                                             :gas (+ gas 1500)
                                                             :supply [0 1500]}))]
                  (true? (production/can-afford-unit? state-rich unit-type)))))

(defspec can-afford-nothing-when-poor
  (prop/for-all [state gen/state
                 unit-type gen/unit-type]
                (let [state-poor (update state :resources (fn [{minerals :minerals
                                                                gas :gas
                                                                [used total] :supply}]
                                                            {:minerals (- minerals 1500)
                                                             :gas (- gas 1500)
                                                             :supply [0 0]}))]
                  (false? (production/can-afford-unit? state-poor unit-type)))))