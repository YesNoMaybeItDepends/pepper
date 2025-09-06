(ns pepper.bot.jobs.attack-move
  (:require [pepper.bot.job :as job]
            [pepper.api :as api]
            [pepper.game.position :as position])
  (:import [bwapi Unit Game Position]))

(defn target-position [job]
  (:target-position job))

(defn yay!
  [game job]
  job)

(defn go-there! [api job]
  (let [frame (Game/.getFrameCount (api/game api))
        unit (Game/.getUnit (api/game api) (job/unit-id job))
        target-position (position/->position (target-position job))
        success? (Unit/.attack unit target-position)]
    (if success?
      (assoc job
             :frame-issued-attack-move-command frame
             :action yay!)
      (do
        (println "Can attack move? "  (job/unit-id job) (Unit/.canAttackMove unit))
        job))))

(defn job [unit-id target-position]
  {:job :attack-move
   :unit-id unit-id
   :target-position target-position
   :action go-there!})