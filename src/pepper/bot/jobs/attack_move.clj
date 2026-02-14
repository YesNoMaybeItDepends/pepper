(ns pepper.bot.jobs.attack-move
  (:require
   [pepper.utils.logging :as logging]
   [pepper.api :as api]
   [pepper.bot.job :as job]
   [pepper.game.position :as position]
   [pepper.game.unit-type :as unit-type])
  (:import
   [bwapi Game Player Unit Position]))

(defn target-position [job]
  (:target-position job))

(defn target-unit-id [job]
  (:target-unit-id job))

(defn yay!
  [job api]
  job)

(defn maybe-heal! [job api]
  (let [game (api/game api)
        frame (Game/.getFrameCount game)
        unit (Game/.getUnit game (job/unit-id job))
        wounded-marines (filterv (every-pred #(Unit/.exists %)
                                             #(= (Unit/.getType %)
                                                 bwapi.UnitType/Terran_Marine)
                                             #(< (Unit/.getHitPoints %)
                                                 (Unit/.getInitialHitPoints %)))
                                 (Player/.getUnits (Game/.self game)))
        ready? (<= (or (:wait-until-frame job) frame) frame)]
    (if (and (not-empty wounded-marines) ready?)
      (do (Unit/.attack unit (Unit/.getPosition (rand-nth wounded-marines)))
          (assoc job :wait-until-frame (+ frame 120)))
      (if ready?
        (assoc job :step :go-there!)
        job))))

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
  (let [game (api/game api)
        frame (Game/.getFrameCount game)
        unit (Game/.getUnit game (job/unit-id job))
        unit-type (Unit/.getType unit)
        target-unit (when (target-unit-id job) (Game/.getUnit game (target-unit-id job)))
        ^Position target-position (when (target-position job) (position/->position (target-position job)))
        success? (cond
                   (and target-unit
                        (Unit/.exists target-unit)
                        (Unit/.isInWeaponRange unit target-unit)
                        (Unit/.isInWeaponRange target-unit unit)
                        (Unit/.canAttackUnit unit target-unit)) (Unit/.attack unit target-unit)
                   target-position (Unit/.attack unit target-position)
                   :else nil)]
    (if success?
      (assoc job
             :frame-issued-attack-move-command frame
             :step (if (not= :medic (unit-type/object->keyword unit-type))
                     :maybe-stim!
                     (if (= :medic (unit-type/object->keyword unit-type))
                       :maybe-heal!
                       :go-there!)))
      (if (or (not (Unit/.exists unit))
              (not target-position))
        (job/set-completed job)
        (logging/log {:event :unexpected :data job :msg "attack-move job that didn't succeed and at least has either an existing unit or a position. AKA What?!"})))))

(defn xform [[job api]]
  (case (:step job)
    :go-there! (#'go-there! job api)
    :maybe-stim! (#'maybe-stim! job api)
    :maybe-heal! (#'maybe-heal! job api)
    :yay! (#'yay! job api)))

(defn job [unit-id target-position opts]
  {:job :attack-move
   :xform-id :attack-move
   :step :go-there!
   :unit-id unit-id
   :target-position target-position
   :target-unit-id (:target-unit-id opts)})