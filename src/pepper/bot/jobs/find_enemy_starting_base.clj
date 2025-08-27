(ns pepper.bot.jobs.find-enemy-starting-base
  (:require
   [pepper.bot.job :as job]
   [pepper.api :as api]
   [pepper.game.position :as position])
  (:import
   [bwapi Game TilePosition Unit]))

(defn starting-base-to-scout [job]
  (:starting-base-to-scout job))

(defn yay!
  "TODO: we need to notify the state of scouting results. 
   no channels to pass messages, so may need to pass the state and update from here"
  [game job]
  job)

(defn go-there! [api job]
  (let [frame (Game/.getFrameCount (api/get-game api))
        worker (Game/.getUnit (api/get-game api) (job/unit-id job))
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