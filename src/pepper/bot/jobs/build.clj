(ns pepper.bot.jobs.build
  (:require [pepper.bot.job :as job]
            [pepper.api :as api]
            [pepper.game.position :as position]
            [pepper.game.unit-type :as unit-type])
  (:import [bwapi Game Unit Player]))

(defn building [job]
  (:building job))

(defn build-tile [job]
  (:build-tile job))

(defn frame-got-build-tile [job]
  (:frame-got-build-tile job))

(defn frame-issued-build-command [job]
  (:frame-issued-build-command job))

(defn frame-started-building [job]
  (:frame-started-building job))

(declare get-build-tile!)

(defn is-building?! [api job]
  (let [worker (Game/.getUnit (api/get-game api) (job/unit-id job))
        started? (frame-started-building job)
        building? (Unit/.isConstructing worker)
        status [started? building?]]
    (cond
      (job/started-working? status) (assoc job :frame-started-building (Game/.getFrameCount (api/get-game api)))
      (job/stopped-working? status) (job/set-completed job)
      :else job)))

(defn go-build! [api job]
  (let [worker (Game/.getUnit (api/get-game api) (job/unit-id job))
        building (unit-type/keyword->object (building job))
        tile (position/->bwapi (build-tile job) :tile-position)
        success? (Unit/.build worker building tile)]
    (if success?
      (assoc job
             :frame-issued-build-command (Game/.getFrameCount (api/get-game api))
             :action is-building?!)
      (assoc job
             :times-retried ((fnil inc 0) (:times-retried job))
             :action get-build-tile!
             :build-tile nil
             :frame-got-build-tile nil))))

(defn get-build-tile! [api job]
  (let [building (unit-type/keyword->object (building job))
        worker (Game/.getUnit (api/get-game api) (job/unit-id job))
        starting-tile (Player/.getStartLocation (Game/.self (api/get-game api)))
        build-tile (Game/.getBuildLocation (api/get-game api) building starting-tile 20)]
    (if build-tile
      (assoc job
             :build-tile (position/->data build-tile)
             :frame-got-build-tile (Game/.getFrameCount (api/get-game api))
             :action go-build!)
      job)))

(defn job [unit-id unit-type]
  {:job :build
   :building unit-type
   :action get-build-tile!
   :unit-id unit-id})

;; problems
;; sometimes workers get conflicting build locations (probably)
;; so they never try building ever again, very sad
;;
;; sometimes the building command doesnt succeed, why?
;; could it be because the game was paused or something?