(ns pepper.bot.jobs.gather
  (:require
   [pepper.bot.job :as job]
   [pepper.api :as api])
  (:import
   [bwapi Game Unit]))

(declare go-mine!)

(defn mineral-field-id [job]
  (:mineral-field-id job))

(defn set-frame-started-gathering-minerals [job frame]
  (assoc job :frame-started-gathering-minerals frame))

(defn frame-started-gathering-minerals [job]
  (:frame-started-gathering-minerals job))

(defn set-frame-issued-gather-command [job frame]
  (assoc job :frame-issued-gather-command frame))

(defn frame-issued-gather-command [job]
  (:frame-issued-gather-command job))

(defn is-gathering-minerals?! [api job]
  (let [game (api/game api)
        worker (Game/.getUnit game (job/unit-id job))
        frame (Game/.getFrameCount game)
        already-started? (frame-started-gathering-minerals job)
        gathering? (Unit/.isGatheringMinerals worker)
        job-status [already-started? gathering?]]
    (cond
      (job/started-working? job-status) (set-frame-started-gathering-minerals job frame)
      (job/stopped-working? job-status) (job/set-completed job)
      (job/is-working? job-status)      job
      :else (job/set-completed job))))  ;; something went wrong

(defn go-mine! [api job]
  (let [game (api/game api)
        frame (Game/.getFrameCount game)
        worker (Game/.getUnit game (job/unit-id job))
        mineral-field (Game/.getUnit game (mineral-field-id job))]
    (Unit/.gather worker mineral-field)
    (-> job
        (assoc :action #'is-gathering-minerals?!)
        (set-frame-issued-gather-command frame))))

(defn mining-job [[worker-id mineral-field-id]]
  {:job :mining
   ;;  :steps {:go-mine :mining
   ;;          :mining :done}
   :action #'go-mine!
   :unit-id worker-id
   :mineral-field-id mineral-field-id})