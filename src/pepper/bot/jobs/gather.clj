(ns pepper.bot.jobs.gather
  (:require
   [pepper.bot.job :as job]
   [pepper.api :as api])
  (:import
   [bwapi Game Unit]))

(defn is-gathering-minerals?! [api job]
  (let [worker (Game/.getUnit (api/get-game api) (:unit-id job))
        started? (:frame-started-gathering-minerals job)
        gathering? (Unit/.isGatheringMinerals worker)
        status [started? gathering?]]
    (cond
      (job/started-working? status) (assoc job :frame-started-gathering-minerals (Game/.getFrameCount (api/get-game api)))
      (job/stopped-working? status) (job/set-completed job)
      :else job)))

(defn go-mine! [api job]
  (let [worker (Game/.getUnit (api/get-game api) (:unit-id job))
        mineral-field (Game/.getUnit (api/get-game api) (:mineral-field-id job))
        success? (Unit/.gather worker mineral-field)]
    (if success?
      (assoc job
             :action is-gathering-minerals?!
             :frame-issued-gather-command (Game/.getFrameCount (api/get-game api)))
      job)))

(defn mining-job [[worker-id mineral-field-id]]
  {:job :mining
   ;;  :steps {:go-mine :mining
   ;;          :mining :done}
   :action go-mine!
   :unit-id worker-id
   :mineral-field-id mineral-field-id})