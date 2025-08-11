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

(defn assign-random-mineral
  [mineral-fields]
  (fn [worker]
    [worker (rand-nth mineral-fields)]))

(defn started-gathering? [[started? gathering?]]
  (and (not started?) gathering?))

(defn stopped-gathering? [[started? gathering?]]
  (and started? (not gathering?)))

(defn is-gathering-minerals?! [game job]
  (let [worker (Game/.getUnit game (:unit-id job))
        started? (:frame-started-gathering-minerals job)
        gathering? (Unit/.isGatheringMinerals worker)
        status [started? gathering?]]
    (cond
      (started-gathering? status) (assoc job :frame-started-gathering-minerals (Game/.getFrameCount game))
      (stopped-gathering? status) (jobs/mark-job-completed job)
      :else job)))

(defn go-mine! [game job]
  (let [worker (Game/.getUnit game (:unit-id job))
        mineral-field (Game/.getUnit game (:mineral-field-id job))
        success? (Unit/.gather worker mineral-field)]
    (if success?
      (assoc job
             :action is-gathering-minerals?!
             :frame-issued-gather-command (Game/.getFrameCount game))
      job)))

(defn mining-job [[worker-id mineral-field-id]]
  {:job :mining
   ;;  :steps {:go-mine :mining
   ;;          :mining :done}
   :action go-mine!
   :unit-id worker-id
   :mineral-field-id mineral-field-id})

(defn process-idle-workers [state]
  (let [idle-workers (map :id (unit/get-idle-workers state))
        mineral-fields (map :id (unit/get-mineral-fields state))
        jobs-to-update (->> (map (assign-random-mineral mineral-fields)
                                 idle-workers)
                            (map mining-job))]
    (reduce jobs/assign-unit-job state jobs-to-update)))