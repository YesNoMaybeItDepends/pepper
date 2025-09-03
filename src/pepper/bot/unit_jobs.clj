(ns pepper.bot.unit-jobs
  (:require
   [pepper.bot.job :as job]
   [pepper.game.unit :as unit]))

(defn get-job-by-unit-id [unit-jobs unit-id]
  (get unit-jobs unit-id))

(defn set-unit-job [unit-jobs unit-job]
  (assoc unit-jobs (job/unit-id unit-job) unit-job))

(defn clear-unit-job [unit-jobs unit-job]
  (dissoc unit-jobs (job/unit-id unit-job)))

(defn units->jobs [jobs units]
  (mapv (fn [unit]
          (get-job-by-unit-id jobs (unit/id unit))) units))

(defn unit-ids->jobs [jobs unit-ids]
  (mapv (fn [unit-id]
          (get-job-by-unit-id jobs unit-id)) unit-ids))

(defn group-jobs-by-type [jobs]
  (group-by job/type jobs))

(defn unit-has-job? [jobs unit-id] ;; employed?
  (some? (get-job-by-unit-id jobs unit-id)))

;; (defn job-unit? [job unit]
;;   (= (job/unit-id job) (unit/id unit)))

;; (defn unit-has-any-job? [unit jobs]
;;   (some #(job-unit? % unit) jobs))

(defn unit-has-no-job? [jobs unit-id]
  ((complement unit-has-job?) jobs unit-id))

;; ;; (defn units-with-no-jobs [units unit-jobs]
;; ;;   (filterv #(not (unit-has-any-job? % unit-jobs)) units))

(defn execute-jobs! [unit-jobs api]
  (reduce-kv
   (fn [m id unit-job]
     (assoc m id (job/process-job! unit-job api)))
   {}
   unit-jobs))

(defn update-on-frame [[unit-jobs messages] api]
  (let [[new-jobs messages] (split-with job/job? (or messages []))
        unit-jobs (reduce set-unit-job unit-jobs new-jobs)
        unit-jobs (execute-jobs! unit-jobs api)]
    [unit-jobs (into [] messages)]))