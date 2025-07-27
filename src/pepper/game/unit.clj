(ns pepper.game.unit
  (:refer-clojure :exclude [type])
  (:require [pepper.game.player :as player])
  (:import
   (bwapi UnitType)))

(def unit-keys #{:id :player-id :type :exists? :idle? :frame-discovered :last-frame-updated})

(defn get-units-by-id [state]
  (or (:units-by-id state)
      {}))

(defn update-unit-by-id [units-by-id unit]
  (update units-by-id (:id unit) merge unit))

(defn update-units-by-id [units-by-id units]
  (reduce update-unit-by-id units-by-id units))

(defn parse-unit
  "Reads a bwapi unit with a bwapi game"
  [game]
  (fn [unit]
    (-> {}
        (assoc :id (bwapi.Unit/.getID unit))
        (assoc :exists? (bwapi.Unit/.exists unit))
        (assoc :last-frame-updated (bwapi.Game/.getFrameCount game))
        (assoc :player-id (bwapi.Player/.getID (bwapi.Unit/.getPlayer unit)))
        (assoc :type (bwapi.Unit/.getType unit)) ;; TODO: convert unit type
        (assoc :idle? (bwapi.Unit/.isIdle unit)))))

(def mineral-types
  #{:mineral
    UnitType/Resource_Mineral_Field
    UnitType/Resource_Mineral_Field_Type_2
    UnitType/Resource_Mineral_Field_Type_3})

(defn mineral? [unit]
  (contains? mineral-types (:type unit)))

(def worker-types
  #{:scv :probe :drone
    UnitType/Terran_SCV
    UnitType/Protoss_Probe
    UnitType/Zerg_Drone})

(defn worker? [unit]
  (contains? worker-types (:type unit)))

(def resource-depot-types
  #{:command-center UnitType/Terran_Command_Center
    :nexus UnitType/Protoss_Nexus
    :hatchery UnitType/Zerg_Hatchery
    :lair UnitType/Zerg_Lair
    :hive UnitType/Zerg_Hive})

(defn resource-depot?
  "Workers can drop resources at resource depots"
  [unit]
  (contains? resource-depot-types (:type unit)))

(defn new-unit? [unit]
  (nil? (:frame-discovered unit)))