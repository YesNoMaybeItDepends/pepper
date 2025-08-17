(ns pepper.game.jobs.find-enemy-starting-base
  (:require
   [pepper.game.job :as job]
   [pepper.game.position :as position])
  (:import
   [bwapi Game TilePosition Unit]))

(defn starting-base-to-scout [job]
  (:starting-base-to-scout job))

(defn yay! [game job]
  job)

(defn go-there! [game job]
  (let [frame (Game/.getFrameCount game)
        worker (Game/.getUnit game (job/unit-id job))
        base (starting-base-to-scout job)
        tile (position/->bwapi base :tile-position)
        position (TilePosition/.toPosition tile)
        ack (Unit/.move worker position)]
    (if ack
      (assoc job
             :frame-issued-move-command frame
             :target-position position
             :action yay!)
      job)))

(defn job [starting-base-to-scout scout-id]
  {:job :find-enemy-starting-base
   :unit-id scout-id
   :starting-base-to-scout starting-base-to-scout
   :action go-there!})