(ns pepper.game.macro
  (:require [pepper.game.unit :as unit])
  (:import (bwapi Unit Game)))

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

(defn get-idle-workers [state]
  (->> (get-workers state)
       (filter :idle?)))

(defn get-minerals [state]
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

(defn assign-unit-job [state job]
  (update-in state [:unit-jobs] assoc (:unit-id job) job))

(defn process-idle-workers [state]
  (let [idle-workers (map :id (get-idle-workers state))
        mineral-fields (map :id (get-minerals state))
        jobs-to-update (->> (map (assign-random-mineral mineral-fields)
                                 idle-workers)
                            (map mining-job))]
    (reduce assign-unit-job state jobs-to-update)))

(defn process-macro [state]
  (-> state
      process-idle-workers))

(defn get-unit-jobs [state]
  (vals (:unit-jobs state)))

(defn delete-job [state job]
  (update-in state [:unit-jobs] dissoc (:unit-id job)))

(defn execute-job!
  ([game]
   (fn [job]
     (assoc job
            :result ((:action job) game job)
            :run? true))))

(defn dispatch-jobs! [game jobs]
  (mapv (execute-job! game) jobs))

(defn filter-pending-jobs [jobs]
  (filterv (complement :run?) jobs))

(defn process-jobs
  "Jobs in general should probably be in their own namespace"
  [state game]
  (update
   state :unit-jobs
   #(update-vals % (fn [job]
                     (if (:run? job)
                       job
                       ((execute-job! game) job))))))