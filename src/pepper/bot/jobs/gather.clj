(ns pepper.bot.jobs.gather
  (:require
   [pepper.game.job :as job])
  (:import
   [bwapi Game Unit]))

(defn is-gathering-minerals?! [game job]
  (let [worker (Game/.getUnit game (:unit-id job))
        started? (:frame-started-gathering-minerals job)
        gathering? (Unit/.isGatheringMinerals worker)
        status [started? gathering?]]
    (cond
      (job/started-working? status) (assoc job :frame-started-gathering-minerals (Game/.getFrameCount game))
      (job/stopped-working? status) (job/set-completed job)
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