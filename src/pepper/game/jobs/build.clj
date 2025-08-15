(ns pepper.game.jobs.build
  (:require [pepper.game.job :as job]
            [pepper.game.position :as position]
            [pepper.game.unit-type :as unit-type])
  (:import [bwapi Game Unit Player]))

(defn building [job]
  (:building job))

(defn build-location [job]
  (:build-location job))

(defn frame-got-build-location [job]
  (:frame-got-build-location job))

(defn frame-issued-build-command [job]
  (:frame-issued-build-command job))

(defn frame-started-building [job]
  (:frame-started-building job))

(declare get-build-location!)

(defn is-building?! [game job]
  (let [worker (Game/.getUnit game (job/unit-id job))
        started? (frame-started-building job)
        building? (Unit/.isConstructing worker)
        status [started? building?]]
    (cond
      (job/started-working? status) (assoc job :frame-started-building (Game/.getFrameCount game))
      (job/stopped-working? status) (job/set-completed job)
      :else job)))

(defn go-build! [game job]
  (let [worker (Game/.getUnit game (job/unit-id job))
        building (unit-type/keyword->object (building job))
        position (position/->bwapi (build-location job))
        success? (Unit/.build worker building position)]
    (if success?
      (assoc job
             :frame-issued-build-command (Game/.getFrameCount game))
      (assoc job
             :action get-build-location!
             :build-location nil
             :frame-got-build-location nil))))

(defn get-build-location! [game job]
  (let [building (unit-type/keyword->object (building job))
        worker (Game/.getUnit game (job/unit-id job))
        starting-location (Player/.getStartLocation (Game/.self game))
        build-location (Game/.getBuildLocation game building starting-location)]
    (if build-location
      (assoc job
             :build-location (position/->data build-location)
             :frame-got-build-location (Game/.getFrameCount game)
             :action go-build!)
      job)))

(defn job [unit-id unit-type]
  {:job :build
   :building unit-type
   :action get-build-location!
   :unit-id unit-id})

;; problems
;; sometimes workers get conflicting build locations (probably)
;; so they never try building ever again, very sad