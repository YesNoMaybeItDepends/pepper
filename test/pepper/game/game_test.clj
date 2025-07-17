(ns pepper.game.game-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.test.alpha :as st]
            [clojure.repl :refer :all]
            [pepper.game.game :as game]
            [pepper.mocking :as mocking])
  (:import (bwapi Game Unit UnitType)))

(st/instrument)

(defn mock-unit [id]
  (mocking/mock Unit [[#(Unit/.getID %) (int id)]
                      [#(Unit/.isFlying %) true]
                      [#(Unit/.getType %) UnitType/Terran_SCV]]))

(defn mock-game [units]
  (mocking/mock Game [[#(Game/.getAllUnits %) units]]))

(deftest game-state-test
  (testing "By default contains units by id"
    (is (contains? (game/get-state {})
                   :game/units-by-id)))

  (testing "By default units by id is a map"
    (is (= (-> (game/get-state {})
               :game/units-by-id)
           {})))

  (testing "Units by id can be updated"
    (is (= (-> (game/get-state {:game/units-by-id {1 {:unit/id 1}}})
               :game/units-by-id)
           {1 {:unit/id 1}})))

  (testing "By default contains new units by id"
    (is (contains? (game/get-state {})
                   :game/new-units-by-id)))

  (testing "By default units by id is a set"
    (is (= (-> (game/get-state {})
               :game/new-units-by-id)
           #{})))

  (testing "New units can be found"
    (let [units [(mock-unit 3)]
          game (mock-game units)
          state (game/get-state {:game/units-by-id {1 {:unit/id 1}}})]
      (is (= (game/find-new-units state game)
             #{3}))))

  (testing "New units by id can be updated"
    (let [units [(mock-unit 3)]
          game (mock-game units)
          state (game/get-state {:game/units-by-id {1 {:unit/id 1}}})]
      (is (= (-> (game/get-state
                  {:game/new-units-by-id (game/find-new-units state game)})
                 :game/new-units-by-id)
             #{3})))))



