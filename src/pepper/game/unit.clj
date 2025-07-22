(ns pepper.game.unit
  (:import
   (bwapi Unit UnitType)))

(defn id [unit]
  (cond
    (int? unit) unit
    (map? unit) (:unit/id unit)
    (instance? Unit unit) (Unit/.getID unit)))

(defn exists? [unit]
  (if (map? unit)
    (:unit/exists? unit)
    (Unit/.exists unit)))

(def worker-unit-types #{:scv :probe :drone
                         UnitType/Terran_SCV
                         UnitType/Protoss_Probe
                         UnitType/Zerg_Drone})

(defn worker? [unit]
  (some? (worker-unit-types
          (if (map? unit)
            (:unit/unit-type unit)
            (Unit/.getType unit)))))

(defn player [unit]
  (if (map? unit)
    (:unit/player unit)
    (Unit/.getPlayer unit)))

(defn with-frame-discovered [unit frame]
  (assoc unit :unit/frame-discovered frame))

(defn with-last-frame-updated [unit frame]
  (assoc unit :unit/last-frame-updated frame))

(defn init [unit-id]
  {:unit/id unit-id})

(defn discover-new-unit [unit-id frame]
  (-> (init unit-id)
      (with-frame-discovered frame)
      (with-last-frame-updated frame)))