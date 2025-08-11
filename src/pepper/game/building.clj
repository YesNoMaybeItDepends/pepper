(ns pepper.game.building
  (:require [pepper.game.resources :as resources]
            [pepper.game.auto-supply :as auto-supply]
            [pepper.game.unit :as unit]
            [pepper.game.jobs :as jobs])
  (:import [bwapi Game Unit UnitType Player TilePosition]))

(defn api-position->position [api-position]
  [(.getX api-position)
   (.getY api-position)])

(defn position->api-position [[x y]]
  (TilePosition. x y))

(defn to-build [job]
  (:to-build job))

(defn build-location [job]
  (:build-location job))

(defn frame-got-build-location [job]
  (:frame-got-build-location job))

(defn frame-issued-build-command [job]
  (:frame-issued-build-command job))

(defn frame-started-building [job]
  (:frame-started-building job))

(defn started-building? [[started? building?]]
  (and (not started?) building?))

(defn stopped-building? [[started? building?]]
  (and started? (not building?)))

(declare get-build-location!)

(defn is-building?! [game job]
  (let [worker (Game/.getUnit game (jobs/unit-id job))
        started? (frame-started-building job)
        building? (Unit/.isConstructing worker)
        status [started? building?]]
    (cond
      (started-building? status) (assoc job :frame-started-building (Game/.getFrameCount game))
      (stopped-building? status) (jobs/mark-job-completed job)
      :else job)))

(defn go-build! [game job]
  (let [worker (Game/.getUnit game (jobs/unit-id job))
        building (to-build job)
        position (position->api-position (build-location job))
        success? (Unit/.build worker building position)]
    (if success?
      (assoc job
             :frame-issued-build-command (Game/.getFrameCount game))
      (assoc job
             :action get-build-location!
             :build-location nil
             :frame-got-build-location nil))))

(defn get-build-location! [game job]
  (let [building (to-build job)
        worker (Game/.getUnit game (jobs/unit-id job))
        starting-location (Player/.getStartLocation (Game/.self game))
        build-location (Game/.getBuildLocation game building starting-location)]
    (if build-location
      (assoc job
             :build-location (api-position->position build-location)
             :frame-got-build-location (Game/.getFrameCount game)
             :action go-build!)
      job)))

(defn build-supply-depot-job [unit-id]
  {:job :build-supply-depot
   :to-build UnitType/Terran_Supply_Depot
   :action get-build-location!
   :unit-id unit-id})

(defn process-building [state]
  (if (and (auto-supply/need-supply? state)
           (auto-supply/can-afford? state)
           (not (auto-supply/building-supply? state)))
    (let [worker (unit/get-idle-or-mining-worker state)]
      (jobs/assign-unit-job state (build-supply-depot-job (unit/id worker))))
    state))