(ns pepper.bot.jobs.attack-move
  (:require [pepper.bot.job :as job]
            [pepper.api :as api]
            [pepper.game.position :as position])
  (:import [bwapi Unit Game Position]))

(defn target-position [job]
  (:target-position job))

(declare go-there!)

(defn yay!
  [job api]
  job)

(defn maybe-stim! [job api]
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
          (assoc job :step :go-there!))
      job)))

(defn go-there! [job api]
  (let [frame (Game/.getFrameCount (api/game api))
        unit (Game/.getUnit (api/game api) (job/unit-id job))
        target-position (when (target-position job) (position/->position (target-position job)))
        success? (when target-position (Unit/.attack unit target-position))]
    (if success?
      (assoc job
             :frame-issued-attack-move-command frame
             :step :maybe-stim!)
      (if (not (Unit/.exists unit))
        (job/set-completed job)
        (println "what?!")))))

(defn xform [[job api]]
  (case (:step job)
    :go-there! (#'go-there! job api)
    :maybe-stim! (#'maybe-stim! job api)
    :yay! (#'yay! job api)))

(defn job [unit-id target-position]
  (job/register-xform! :attack-move #'xform)
  {:job :attack-move
   :xform-id :attack-move
   :step :go-there!
   :unit-id unit-id
   :target-position target-position})