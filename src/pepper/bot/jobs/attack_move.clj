(ns pepper.bot.jobs.attack-move
  (:require [pepper.bot.job :as job]
            [pepper.api :as api]
            [pepper.game.position :as position])
  (:import [bwapi Unit Game Position]))

(defn target-position [job]
  (:target-position job))

(declare go-there!)

(defn yay!
  [game job]
  job)

(defn maybe-stim! [api job]
  (let [frame (Game/.getFrameCount (api/game api))
        unit (Game/.getUnit (api/game api) (job/unit-id job))
        can-stim? (Unit/.canUseTech unit bwapi.TechType/Stim_Packs)
        stimmed? (Unit/.isStimmed unit)
        starting-attack? (Unit/.isStartingAttack unit)
        attack-frame? (Unit/.isAttackFrame unit)
        healthy? (< 15 (Unit/.getHitPoints unit))
        under-attack? (Unit/.isUnderAttack unit)]
    (if (and unit
             can-stim?
             (not stimmed?)
             healthy?
             (or starting-attack? attack-frame? under-attack?))
      (do (Unit/.useTech unit bwapi.TechType/Stim_Packs)
          (assoc job :action go-there!))
      job)))

(defn go-there! [api job]
  (let [frame (Game/.getFrameCount (api/game api))
        unit (Game/.getUnit (api/game api) (job/unit-id job))
        target-position (when (target-position job) (position/->position (target-position job)))
        success? (when target-position (Unit/.attack unit target-position))]
    (if success?
      (assoc job
             :frame-issued-attack-move-command frame
             :action maybe-stim!)
      (do (println "attack-move job target-position is nil, what happened?")
          job))))

(defn job [unit-id target-position]
  {:job :attack-move
   :unit-id unit-id
   :target-position target-position
   :action go-there!})