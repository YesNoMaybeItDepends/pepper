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
  (let [frame (Game/.getFrameCount (api/game api))
        worker (Game/.getUnit (api/game api) (job/unit-id job))
        pos (position/->position (starting-base-to-scout job))
        ack (Unit/.move worker pos)]
    (if ack
      (assoc job
             :frame-issued-move-command frame
             :target-position pos
             :action yay!)
      job)))

(defn job [starting-base-to-scout scout-id]
  {:job :find-enemy-starting-base
   :unit-id scout-id
   :starting-base-to-scout starting-base-to-scout
   :action go-there!})