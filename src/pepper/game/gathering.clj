(ns pepper.game.gathering
  (:require
   [pepper.game.jobs :as jobs]
   [pepper.game.unit :as unit]
   [pepper.game.jobs.gather :as gather]))

(defn init-gathering [state]
  {:workers #{}
   :mineral-fields #{}
   :geysers #{}
   :resource-depots #{}
   :supply-depots #{}
   :macro-orders #{}})

(defn assign-random-mineral
  [mineral-fields]
  (fn [worker]
    [worker (rand-nth mineral-fields)]))

(defn process-idle-workers [state]
  (let [idle-workers (map :id (unit/get-idle-workers state))
        mineral-fields (map :id (unit/get-mineral-fields state))
        new-jobs (->> (map (assign-random-mineral mineral-fields)
                           idle-workers)
                      (map gather/mining-job))]
    (reduce jobs/assign-unit-job state new-jobs)))