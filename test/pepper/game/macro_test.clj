(ns pepper.game.macro-test
  {:clj-kondo/config '{:lint-as {clojure.test.check.clojure-test/defspec clj-kondo.lint-as/def-catch-all}}}
  (:require
   [clojure.spec.test.alpha :as st]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [pepper.game.jobs :as jobs]
   [pepper.game.macro :as macro])
  (:import
   (bwapi UnitType)))

(st/instrument)

(deftest macro-test
  (let [state {:units-by-id {1 {:id 1
                                :type :mineral-field
                                :idle? true}
                             2 {:id 2
                                :type :scv
                                :idle? false}
                             3 {:id 3
                                :type :scv
                                :idle? true}
                             4 {:id 4
                                :type :bro
                                :idle? false}
                             5 {:id 5
                                :type UnitType/Resource_Mineral_Field
                                :idle? false}
                             6 {:id 6
                                :type UnitType/Resource_Mineral_Field_Type_2
                                :idle? false}
                             7 {:id 7
                                :type UnitType/Resource_Mineral_Field_Type_3
                                :idle? false}}}]
    (testing "We can find our workers"
      (is (= (macro/get-workers state)
             [{:id 2 :type :scv :idle? false} {:id 3 :type :scv :idle? true}])))

    (testing "We can find our idle workers"
      (is (= (macro/get-idle-workers state)
             [{:id 3 :type :scv :idle? true}])))

    (testing "We can assign a unit a new mining job"
      (is (= (jobs/assign-unit-job {} (macro/mining-job [1 2]))
             {:unit-jobs {1 {:job :mining
                             :action macro/go-mine!
                             :unit-id 1
                             :mineral-field-id 2}}})))

    (testing "We can assign a random mineral to a worker"
      (is (= ((macro/assign-random-mineral [2]) 1)
             [1 2])))

    (testing "We can assign random minerals to a sequence of workers"
      (is (= (map (macro/assign-random-mineral [2 2]) [1 1])
             [[1 2]
              [1 2]])))

    (testing "We can get mineral-fields"
      (is (= (macro/get-mineral-fields {:units-by-id {1 {:id 1
                                                         :type :mineral-field}
                                                      2 {:id 2
                                                         :type :scv}}})
             [{:id 1 :type :mineral-field}])))

    (testing "We can process idle workers in a macro step"
      (let [state-1 {:units-by-id {1 {:id 1
                                      :type :scv
                                      :idle? true}
                                   2 {:id 2
                                      :type :mineral-field
                                      :idle? true}}}
            state-2 {:units-by-id {1 {:id 1
                                      :type :scv
                                      :idle? true}
                                   2 {:id 2
                                      :type :mineral-field
                                      :idle? true}}
                     :unit-jobs {1 {:job :mining
                                    :action macro/go-mine!
                                    :unit-id 1
                                    :mineral-field-id 2}}}]
        (is (= (macro/process-idle-workers state-1)
               state-2))))))

;;;; Resources

(def gen-minerals gen/nat)
(def gen-gas gen/nat)
(def gen-supply (gen/fmap
                 (fn [[x y]]
                   [x (* x (+ y 1))])
                 (gen/tuple gen/nat gen/nat)))
(def gen-resources (gen/hash-map
                    :minerals gen-minerals
                    :gas gen-gas
                    :supply gen-supply))
(def gen-unit-type (gen/elements (into [] (.getEnumConstants UnitType))))
(def gen-state (gen/hash-map :resources gen-resources))

(deftest init-resources-works
  (is (= {:resources {:minerals 0
                      :gas 0
                      :supply [0 0]}}
         (macro/init-resources {}))))

(defspec get-minerals-works
  (prop/for-all [state gen-state]
                (let [minerals (-> state
                                   macro/state->resources
                                   macro/resources->minerals)]
                  (and (some? minerals)
                       (int? minerals)))))

(defspec get-gas-works
  (prop/for-all [state gen-state]
                (let [gas (-> state
                              macro/state->resources
                              macro/resources->gas)]
                  (and (some? gas)
                       (int? gas)))))

(defspec get-supply-works
  (prop/for-all [state gen-state]
                (let [[used total] (-> state
                                       macro/state->resources
                                       macro/resources->supply)]
                  ((every-pred some? int?) used total))))

(defspec get-supply-used-works
  (prop/for-all [state gen-state]
                (let [used (-> state
                               macro/state->resources
                               macro/resources->supply
                               macro/supply->supply-used)]
                  ((every-pred some? int?) used))))

(defspec get-supply-total-works
  (prop/for-all [state gen-state]
                (let [total (-> state
                                macro/state->resources
                                macro/resources->supply
                                macro/supply->supply-total)]
                  ((every-pred some? int?) total))))

(defspec can-afford-everything-when-rich
  (prop/for-all [state gen-state
                 unit-type gen-unit-type]
                (let [state-rich (update state :resources (fn [{minerals :minerals
                                                                gas :gas
                                                                [used total] :supply}]
                                                            {:minerals (+ minerals 1500)
                                                             :gas (+ gas 1500)
                                                             :supply [0 1500]}))]
                  (true? (macro/can-afford? state-rich unit-type)))))

(defspec can-afford-nothing-when-poor
  (prop/for-all [state gen-state
                 unit-type gen-unit-type]
                (let [state-poor (update state :resources (fn [{minerals :minerals
                                                                gas :gas
                                                                [used total] :supply}]
                                                            {:minerals (- minerals 1500)
                                                             :gas (- gas 1500)
                                                             :supply [0 0]}))]
                  (false? (macro/can-afford? state-poor unit-type)))))