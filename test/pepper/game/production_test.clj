(ns pepper.game.production-test
  {:clj-kondo/config '{:lint-as {clojure.test.check.clojure-test/defspec clj-kondo.lint-as/def-catch-all}}}
  (:require
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.properties :as prop]
   [pepper.game.resources :as resources]
   [pepper.game.unit-type :as unit-type]
   [pepper.generators :as gen]))

(defspec can-afford-everything-when-rich
  (prop/for-all [state gen/state
                 unit-type gen/unit-type]
                (let [state-rich (update state :resources (fn [{minerals :minerals
                                                                gas :gas
                                                                [used total] :supply}]
                                                            {:minerals (+ minerals 1500)
                                                             :gas (+ gas 1500)
                                                             :supply [0 1500]}))]
                  (true? (resources/can-afford? state-rich (unit-type/cost (unit-type/object->keyword unit-type)))))))

(defspec can-afford-nothing-when-poor
  (prop/for-all [state gen/state
                 unit-type gen/unit-type]
                (let [state-poor (update state :resources (fn [{minerals :minerals
                                                                gas :gas
                                                                [used total] :supply}]
                                                            {:minerals (- minerals 1500)
                                                             :gas (- gas 1500)
                                                             :supply [0 0]}))]
                  (false? (resources/can-afford? state-poor (unit-type/cost (unit-type/object->keyword unit-type)))))))