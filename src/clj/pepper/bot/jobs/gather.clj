(ns pepper.bot.jobs.gather
  (:require
   [pepper.bot.job :as job]
   [pepper.api :as api])
  (:import
   [bwapi Game Unit]))

(declare go-gather!)

(defn target-id [job]
  (:target-id job))

(defn set-frame-started-gathering [job frame]
  (assoc job :frame-started-gathering frame))

(defn frame-started-gathering [job]
  (:frame-started-gathering job))

(defn set-frame-issued-gather-command [job frame]
  (assoc job :frame-issued-gather-command frame))

(defn frame-issued-gather-command [job]
  (:frame-issued-gather-command job))

(defn gather-job? [job]
  ((job/type? :gather) job))

(defn is-gathering?! [api job]
  (let [game (api/game api)
        worker (Game/.getUnit game (job/unit-id job))
        frame (Game/.getFrameCount game)
        already-started? (frame-started-gathering job)
        gathering? (or (Unit/.isGatheringMinerals worker)
                       (Unit/.isGatheringGas worker))
        job-status [already-started? gathering?]]
    (cond
      (job/started-working? job-status) (set-frame-started-gathering job frame)
      (job/stopped-working? job-status) (job/set-completed job)
      (job/is-working? job-status)      job
      :else                             (job/set-cancelled job))))  ;; something went wrong

(defn go-gather! [api job]
  (let [game (api/game api)
        frame (Game/.getFrameCount game)
        worker (Game/.getUnit game (job/unit-id job))
        target (Game/.getUnit game (target-id job))]
    (Unit/.gather worker target)
    (-> job
        (assoc :action #'is-gathering?!)
        (set-frame-issued-gather-command frame))))

(defn gather-job [[worker-id target-id]]
  {:job :gather
   :action #'go-gather!
   :unit-id worker-id
   :target-id target-id})