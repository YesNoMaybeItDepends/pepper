(ns pepper.game.building
  (:require [pepper.game.auto-supply :as auto-supply]
            [pepper.game.unit :as unit]
            [pepper.game.jobs :as jobs]
            [pepper.game.jobs.build :as build])
  (:import [bwapi UnitType]))

(defn process-building [state]
  (if (and (auto-supply/need-supply? state)
           (auto-supply/can-afford? state)
           (not (auto-supply/building-supply? state)))
    (let [worker (unit/get-idle-or-mining-worker state)]
      (jobs/assign-unit-job state (build/job (unit/id worker) UnitType/Terran_Supply_Depot)))
    state))