(ns pepper.game.jobs.train
  (:require [pepper.game.job :as job]
            [pepper.game.unit-type :as unit-type])
  (:import
   [bwapi Game Unit]))

(declare train!)

(defn frame-issued-train-command [job]
  (:frame-issued-train-command job))

(defn train-command-dropped? [trainee frame-issued-train-command frame]
  (and (not (some? trainee))
       (>= (- frame frame-issued-train-command)
           50)))

(defn training-completed?! [game job]
  (let [trainee (Game/.getUnit game (:requested-id job))
        trainee-completed? (Unit/.isCompleted trainee)]
    (if trainee-completed?
      (job/set-completed job)
      job)))

(defn get-trainee! [game job]
  (let [trainer (Game/.getUnit game (:unit-id job))
        frame (Game/.getFrameCount game)
        trainee (Unit/.getBuildUnit trainer)]
    (if (some? trainee)
      (assoc job
             :requested-id (Unit/.getID trainee)
             :frame-got-trainee-id frame
             :action training-completed?!)
      (if (train-command-dropped? trainee (frame-issued-train-command job) frame)
        (job/set-completed job)
        job))))

(defn train! [game job]
  (let [trainer (Game/.getUnit game (:unit-id job))
        unit-type (unit-type/keyword->object (:requested job))
        success? (Unit/.train trainer unit-type)]
    (if success?
      (assoc job
             :action get-trainee!
             :frame-issued-train-command (Game/.getFrameCount game))
      job)))

(defn job [unit-id unit-type]
  {:job :train
   :requested unit-type
   :action train!
   :unit-id unit-id})

;; issue train command -> is training?
;; or
;; issue train command -> get trainee
;; should schedule a timeout anyways
;;