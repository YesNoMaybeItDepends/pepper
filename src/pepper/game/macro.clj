(ns pepper.game.macro
  (:require
   [pepper.game.auto-supply :as auto-supply]
   [pepper.game.gathering :as gathering]
   [pepper.game.jobs :as jobs]
   [pepper.game.jobs.build :as build]
   [pepper.game.resources :as resources]
   [pepper.game.training :as training]
   [pepper.game.unit :as unit]
   [pepper.game.unit-type :as unit-type]))

(defn already-building? [building state]
  (->> (jobs/get-unit-jobs state)
       (filter #(= (build/building %) building))
       first))

(defn maybe-build-supply [state]
  (if (and (auto-supply/need-supply? state)
           (auto-supply/can-afford? state)
           (not (auto-supply/building-supply? state)))
    (let [worker (unit/get-idle-or-mining-worker state)]
      (jobs/assign-unit-job state (build/job (unit/id worker) :supply-depot)))
    state))

(defn maybe-build-barracks [state]
  (let [budget (resources/get-state-resources-available state)
        cost (unit-type/cost :barracks)
        can-afford? (resources/can-afford?-v2 budget cost)
        some-worker (unit/get-idle-or-mining-worker state)
        already-building? (already-building? :barracks state)]
    (if (and (not already-building?) some-worker can-afford?)
      (jobs/assign-unit-job state (build/job (unit/id some-worker) :barracks))
      state)))

(defn process-macro [state]
  (-> state
      resources/process-resources
      gathering/process-idle-workers
      maybe-build-supply
      maybe-build-barracks
      training/process-idle-command-centers))