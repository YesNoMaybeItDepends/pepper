(ns pepper.bot.jobs.build
  (:require [pepper.bot.job :as job]
            [pepper.api :as api]
            [pepper.game.position :as position]
            [pepper.game.unit-type :as unit-type])
  (:import [bwapi Game Unit Player]))

;; problems
;; sometimes workers get conflicting build locations (probably)
;; so they never try building ever again, very sad

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
(declare go-build!)

(defn is-building?! [api job]
  (let [game (api/game api)
        worker (Game/.getUnit game (job/unit-id job))
        started? (frame-started-building job)
        building? (Unit/.isConstructing worker)
        status [started? building?]]
    (cond
      (job/started-working? status) (assoc job :frame-started-building (Game/.getFrameCount game))
      (job/stopped-working? status) (job/set-completed job)
      (job/is-working? status) job
      :else (job/set-completed job)))) ;; something went wrong

(defn go-build! [api job]
  (let [game (api/game api)
        frame (Game/.getFrameCount game)
        worker (Game/.getUnit game (job/unit-id job))
        building (unit-type/keyword->object (building job))
        tile (position/->bwapi (build-tile job) :tile-position)
        can-build? (Unit/.canBuild worker building tile)]
    (if can-build?
      (do (Unit/.build worker building tile)
          (assoc job
                 :action #'is-building?!
                 :frame-issued-build-command frame))
      (assoc job
             :times-retried ((fnil inc 0) (:times-retried job))
             :action #'get-build-tile!
             :build-tile nil
             :frame-got-build-tile nil))))

(defn get-build-tile! [api job]
  (let [game (api/game api)
        building (unit-type/keyword->object (building job))
        worker (Game/.getUnit game (job/unit-id job))
        starting-tile (Player/.getStartLocation (Game/.self game))
        build-tile (Game/.getBuildLocation game building starting-tile 20)]
    (if build-tile
      (assoc job
             :build-tile (position/->data build-tile)
             :frame-got-build-tile (Game/.getFrameCount game)
             :action #'go-build!)
      job)))

(defn job [unit-id unit-type]
  {:job :build
   :building unit-type
   :action #'get-build-tile!
   :unit-id unit-id})
