(ns pepper.game.unit
  (:refer-clojure :exclude [type])
  (:require [pepper.game.player :as player]
            [pepper.game.jobs :as jobs]
            [pepper.game.job :as job]
            [pepper.game.unit :as unit])
  (:import
   (bwapi UnitType)))

(def unit-keys #{:id :player-id :type :exists? :idle? :frame-discovered :last-frame-updated})

(defn type [unit]
  (cond
    (map? unit) (:type unit)
    (instance? UnitType unit) unit))

(defn id [unit]
  (:id unit))

(defn get-units [state]
  (vals (:units-by-id state)))

(defn get-unit-by-id [state unit-id]
  (get-in state [:units-by-id unit-id]))

(defn update-unit-by-id [units-by-id unit]
  (update units-by-id (:id unit) merge unit))

(defn update-units-by-id [units-by-id units]
  (reduce update-unit-by-id units-by-id units))

(defn parse-unit!
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

(def mineral-field-types
  #{:mineral-field
    UnitType/Resource_Mineral_Field
    UnitType/Resource_Mineral_Field_Type_2
    UnitType/Resource_Mineral_Field_Type_3})

(defn mineral-field? [unit]
  (contains? mineral-field-types (:type unit)))

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

(defn command-center?
  [unit]
  (contains? #{:command-center UnitType/Terran_Command_Center} (type unit)))

(defn new-unit? [unit]
  (nil? (:frame-discovered unit)))

(defn ours? [state unit]
  (= (:player-id unit)
     (player/get-self-id state)))

(defn idle? [unit]
  (:idle? unit))

(defn mineral-cost [unit]
  (let [type (type unit)]
    (assert (not (keyword? type)))
    (UnitType/.mineralPrice type)))

(defn gas-cost [unit]
  (let [type (type unit)]
    (assert (not (keyword? type)))
    (UnitType/.gasPrice type)))

(defn supply-cost [unit]
  (let [type (type unit)]
    (assert (not (keyword? type)))
    (UnitType/.supplyRequired type)))

(defn employed? [state unit]
  (some? (jobs/get-unit-job state (id unit))))

(defn unemployed? [state unit]
  ((complement employed?) state unit))

(defn get-workers [state]
  (->> (get-units state)
       (filter #(ours? state %))
       (filter #(worker? %))))

(defn get-idle-workers [state]
  (->> (get-workers state)
       (filter idle?)))

(defn with-job? [state job-type unit]
  (-> (jobs/get-unit-job state (id unit))
      (job/type? job-type)))

(defn group-workers-by-job [state]
  (->> (get-workers state)
       (group-by #(job/type (jobs/get-unit-job state (id %))))))

(defn get-mineral-fields [state]
  (->> (get-units state)
       (filter mineral-field?)))

(defn get-idle-or-mining-worker [state]
  (let [workers-by-job (group-workers-by-job state)
        worker-id (cond (some? (get workers-by-job nil))
                        (:id (first (get workers-by-job nil)))

                        (some? (:mining workers-by-job))
                        (:id (first (:mining workers-by-job))))]
    (unit/get-unit-by-id state worker-id)))