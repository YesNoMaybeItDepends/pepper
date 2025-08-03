(ns pepper.game.macro
  (:require [pepper.game.unit :as unit]
            [pepper.game.player :as players]
            [pepper.game.jobs :as jobs]
            [pepper.game.player :as player])
  (:import (bwapi Unit Game UnitType)))

;;;; split mining out to harvesting or mining or something

(defn init-macro [state]
  {:workers #{}
   :mineral-fields #{}
   :geysers #{}
   :resource-depots #{}
   :supply-depots #{}
   :macro-orders #{}})

(defn get-workers [state]
  (->> (vals (:units-by-id state))
       (filter #(unit/ours? state %))
       (filter #(unit/worker? %))))

(defn get-command-centers [state]
  (->> (unit/get-units state)
       (filter #(unit/ours? state %))
       (filter #(unit/command-center? %))))

(defn get-idle-workers [state]
  (->> (get-workers state)
       (filter :idle?)))

(defn get-mineral-fields [state]
  (->> (unit/get-units state)
       (filter unit/mineral-field?)))

(defn assign-random-mineral
  [mineral-fields]
  (fn [worker]
    [worker (rand-nth mineral-fields)]))

(defn go-mine! [game job]
  (let [worker (Game/.getUnit game (:unit-id job))
        mineral-field (Game/.getUnit game (:mineral-field-id job))]
    (Unit/.gather worker mineral-field)))

(defn mining-job [[worker-id mineral-field-id]]
  {:job :mining
   ;;  :steps {:go-mine :mining
   ;;          :mining :done}
   :action go-mine!
   :unit-id worker-id
   :mineral-field-id mineral-field-id})

(defn process-idle-workers [state]
  (let [idle-workers (map :id (get-idle-workers state))
        mineral-fields (map :id (get-mineral-fields state))
        jobs-to-update (->> (map (assign-random-mineral mineral-fields)
                                 idle-workers)
                            (map mining-job))]
    (reduce jobs/assign-unit-job state jobs-to-update)))

(defn process-macro [state]
  (-> state
      process-idle-workers))

;;;; Resources
;; Refactor resources so that it does not directly depend on state as much (?)
;; resource state -> update resource state -> get resource state -> ...

(defn get-frame-resources [state]
  (let [self (players/get-self state)]
    (-> {}
        (assoc :minerals-total (players/minerals self))
        (assoc :gas-total (players/gas self))
        (assoc :supply-total (players/supply-total self))
        (assoc :supply-used (players/supply-total self)))))

(defn init-resources [state]
  (assoc state :resources {:gas 0
                           :minerals 0
                           :supply [0 0]}))

(defn get-resources [state]
  (:resources state))

(defn get-minerals [state]
  (:minerals (get-resources state)))

(defn get-gas [state]
  (:gas (get-resources state)))

(defn get-supply [state]
  (:supply (get-resources state)))

(defn get-supply-used [state]
  (let [[used _] (get-supply state)]
    used))

(defn get-supply-total [state]
  (let [[_ total] (get-supply state)]
    total))

(defn get-supply-available [state]
  (let [[used total] (get-supply state)]
    (- total used)))

(defn supply [[used total]]
  {:supply-used used
   :supply-total total})

(defn get-our-minerals [state]
  (players/minerals (players/get-self state)))

(defn get-our-gas [state]
  (players/gas (players/get-self state)))

(defn get-our-supply [state]
  (let [p (players/get-self state)]
    [(players/supply-used p) (players/supply-total p)]))

(defn update-resources [state resources]
  (assoc state :resources resources))

(defn can-afford? [state unit-type]
  (let [resources (get-resources state)
        mineral-cost (unit/mineral-cost unit-type)
        gas-cost (unit/gas-cost unit-type)
        supply-cost (unit/supply-cost unit-type)]
    (and
     (<= mineral-cost (get-minerals state))
     (<= gas-cost (get-gas state))
     (<= supply-cost (get-supply-available state)))))