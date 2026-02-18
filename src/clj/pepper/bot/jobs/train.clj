(ns pepper.bot.jobs.train
  (:require
   [pepper.api :as api]
   [pepper.bot.job :as job]
   [pepper.game.unit-type :as unit-type])
  (:import
   [bwapi Game Unit]))

(declare train!)

(defn frame-issued-train-command [job]
  (:frame-issued-train-command job))

(defn train-command-dropped? [trainee frame frame-issued-train-command]
  (and (not (some? trainee))
       (>= (- frame frame-issued-train-command) 100)))

(defn training-completed?! [api job]
  (let [trainee (Game/.getUnit (api/game api) (:requested-id job))
        trainee-completed? (Unit/.isCompleted trainee)]
    (if trainee-completed?
      (job/set-completed job)
      job)))

(defn get-trainee! [api job]
  (let [trainer (Game/.getUnit (api/game api) (:unit-id job))
        frame (Game/.getFrameCount (api/game api))
        is-training? (Unit/.isTraining trainer)
        trainee (Unit/.getBuildUnit trainer)
        frame-issued-train-command (frame-issued-train-command job)]
    (if (some? trainee)
      (-> job
          (assoc :requested-id (Unit/.getID trainee))
          (assoc :frame-got-trainee-id frame)
          (assoc :action training-completed?!)
          (job/set-cost-paid frame))
      (if (train-command-dropped? trainee frame frame-issued-train-command)
        (job/set-completed job)
        job))))

(defn train! [api job]
  (let [trainer (Game/.getUnit (api/game api) (:unit-id job))
        unit-type (unit-type/keyword->object (:requested job))
        is-training? (Unit/.isTraining trainer)
        frame (Game/.getFrameCount (api/game api))]
    (if (not is-training?)
      (do (Unit/.train trainer unit-type)
          (assoc job
                 :action get-trainee!
                 :frame-issued-train-command frame))
      (assoc job
             :times-tried ((fnil inc 0) (:times-retried job))))))

(defn job [unit-id unit-type]
  {:job :train
   :requested unit-type
   :cost (unit-type/cost unit-type)
   :action train!
   :unit-id unit-id})

;; happyish path
;; is training? -> issue train command -> is training? -> get trainee -> trainee-completed? -> complete