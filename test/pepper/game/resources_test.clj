(ns pepper.game.resources-test
  {:clj-kondo/config '{:lint-as {clojure.test.check.clojure-test/defspec clj-kondo.lint-as/def-catch-all}}}
  (:require
   [clojure.test :refer [deftest is]]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.properties :as prop]
   [pepper.game.resources :as resources]
   [pepper.generators :as gen]))

(deftest init-resources-works
  (is (= {:resources {:minerals 0
                      :gas 0
                      :supply [0 0]}}
         (resources/init-resources {}))))

(defspec get-minerals-works
  (prop/for-all [state gen/state]
                (let [minerals (-> state
                                   resources/get-state-resources
                                   resources/get-minerals)]
                  (and (some? minerals)
                       (int? minerals)))))

(defspec get-gas-works
  (prop/for-all [state gen/state]
                (let [gas (-> state
                              resources/get-state-resources
                              resources/get-gas)]
                  (and (some? gas)
                       (int? gas)))))

(defspec get-supply-works
  (prop/for-all [state gen/state]
                (let [[used total] (-> state
                                       resources/get-state-resources
                                       resources/get-supply)]
                  ((every-pred some? int?) used total))))

(defspec get-supply-used-works
  (prop/for-all [state gen/state]
                (let [[used total] (-> state
                                       resources/get-state-resources
                                       resources/get-supply)]
                  ((every-pred some? int?) used))))

(defspec get-supply-total-works
  (prop/for-all [state gen/state]
                (let [[used total] (-> state
                                       resources/get-state-resources
                                       resources/get-supply)]
                  ((every-pred some? int?) total))))