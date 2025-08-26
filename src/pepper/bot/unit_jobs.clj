(ns pepper.bot.unit-jobs
  (:require [pepper.bot.job :as job]
            [pepper.game.unit :as unit]))

(defn get-unit-job [unit-jobs unit-id]
  (get unit-jobs unit-id))

(defn set-unit-job [unit-jobs unit-job]
  (assoc unit-jobs (job/unit-id unit-job) unit-job))

(defn clear-unit-job [unit-jobs unit-job]
  (dissoc unit-jobs (job/unit-id unit-job)))

(defn units->jobs [jobs units]
  (mapv (fn [unit]
          (get-unit-job jobs (unit/id unit))) units))

(defn unit-ids->jobs [jobs unit-ids]
  (mapv (fn [unit-id]
          (get-unit-job jobs unit-id)) unit-ids))

(defn group-jobs-by-type [jobs]
  (group-by job/type jobs))

(defn unit-has-job? [jobs unit-id] ;; employed?
  (some? (get-unit-job jobs unit-id)))

(defn unit-has-no-job? [jobs unit-id]
  ((complement unit-has-job?) jobs unit-id))

(defn execute-jobs! [unit-jobs api]
  (reduce-kv
   (fn [m id unit-job]
     (assoc m id (job/process-job! unit-job api)))
   {}
   unit-jobs))

(defn update-on-frame [unit-jobs api]
  (execute-jobs! unit-jobs api))