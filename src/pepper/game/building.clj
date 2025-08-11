(ns pepper.game.building
  (:require [pepper.game.resources :as resources]
            [pepper.game.auto-supply :as auto-supply]
            [pepper.game.unit :as unit]
            [pepper.game.jobs :as jobs])
  (:import [bwapi UnitType]))

(defn go-build! [game job])

(defn build-supply-depot-job [unit-id]
  {:job :build-supply-depot
   :to-build UnitType/Terran_Supply_Depot
   :action go-build!
   :unit-id unit-id})

(defn process-building [state]
  (if (and (auto-supply/need-supply? state)
           (auto-supply/can-afford? state))
    (let [worker (unit/get-idle-or-mining-worker state)]
      (jobs/assign-unit-job state (build-supply-depot-job (unit/id worker))))
    state))