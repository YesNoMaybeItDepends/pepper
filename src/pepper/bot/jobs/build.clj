(ns pepper.bot.jobs.build
  (:require [pepper.bot.job :as job]
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
        tile (position/->bwapi (build-tile job) :tile-position)
        success? (Unit/.build worker building tile)]
    (if success?
      (assoc job
             :frame-issued-build-command (Game/.getFrameCount game))
      (assoc job
             :action get-build-tile!
             :build-tile nil
             :frame-got-build-tile nil))))

(defn get-build-tile! [game job]
  (let [building (unit-type/keyword->object (building job))
        worker (Game/.getUnit game (job/unit-id job))
        starting-tile (Player/.getStartLocation (Game/.self game))
        build-tile (Game/.getBuildLocation game building starting-tile)]
    (if build-tile
      (assoc job
             :build-tile (position/->data build-tile)
             :frame-got-build-tile (Game/.getFrameCount game)
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