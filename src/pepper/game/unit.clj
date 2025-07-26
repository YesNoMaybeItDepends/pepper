(ns pepper.game.unit
  (:refer-clojure :exclude [type])
  (:require [pepper.game.player :as player])
  (:import
   (bwapi UnitType)))

(defn id [unit]
  (:unit/id unit))

(defn with-id [unit id]
  (assoc unit :unit/id id))

(defn player-id [unit]
  (:unit/player-id unit))

(defn with-player-id [unit id]
  (assoc unit :unit/player-id id))

(defn type [unit]
  (:unit/type unit))

(defn with-type [unit type]
  (assoc unit :unit/type type))

(defn exists? [unit]
  (:unit/exists? unit))

(defn with-exists? [unit exists?]
  (assoc unit :unit/exists? exists?))

(defn idle? [unit]
  (:unit/idle? unit))

(defn with-idle? [unit idle?]
  (assoc unit :unit/idle? idle?))

(defn frame-discovered [unit]
  (:unit/frame-discovered unit))

(defn with-frame-discovered [unit frame]
  (assoc unit :unit/frame-discovered frame))

(defn last-frame-updated [unit]
  (:unit/last-frame-updated unit))

(defn with-last-frame-updated [unit frame]
  (assoc unit :unit/last-frame-updated frame))

(defn read-game-unit
  "Reads a bwapi unit with a bwapi game"
  [game]
  (fn [unit]
    (-> {}
        (with-id (bwapi.Unit/.getID unit))
        (with-exists? (bwapi.Unit/.exists unit))
        (with-last-frame-updated (bwapi.Game/.getFrameCount game))
        (with-player-id (bwapi.Player/.getID (bwapi.Unit/.getPlayer unit)))
        (with-type (bwapi.Unit/.getType unit)) ;; TODO: convert unit type
        (with-idle? (bwapi.Unit/.isIdle unit)))))

(defn worker? [unit]
  (some? (#{:scv :probe :drone
            UnitType/Terran_SCV
            UnitType/Protoss_Probe
            UnitType/Zerg_Drone}
          (type unit))))