(ns pepper.game.jobs.train
  (:require [pepper.game.job :as job]
            [pepper.game.unit-type :as unit-type])
  (:import
   [bwapi Game Unit]))

(declare train!)

(defn training-completed?! [game job]
  (let [trainee (Game/.getUnit game (:requested-id job))
        trainee-completed? (Unit/.isCompleted trainee)]
    (if trainee-completed?
      (job/set-completed job)
      job)))

(defn get-trainee! [game job]
  (let [trainer (Game/.getUnit game (:unit-id job))]
    (if-some [trainee (Unit/.getBuildUnit trainer)]
      (assoc job
             :requested-id (Unit/.getID trainee)
             :frame-got-trainee-id (Game/.getFrameCount game)
             :action training-completed?!)
      job)))

(defn train! [game job]
  (let [trainer (Game/.getUnit game (:unit-id job))
        unit-type (unit-type/keyword->object (:requested job))
        success? (Unit/.train trainer unit-type)]
    (if success?
      (assoc job
             :action get-trainee!
             :frame-issued-train-command (Game/.getFrameCount game))
      job)))

(defn train-scv-job [unit-id]
  {:job :train-scv
   :requested :scv
   :action train!
   :unit-id unit-id})