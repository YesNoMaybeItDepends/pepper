(ns pepper.game.gathering
  (:require
   [pepper.game.jobs :as jobs]
   [pepper.game.unit :as unit])
  (:import
   [bwapi Game Unit]))

(defn init-gathering [state]
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