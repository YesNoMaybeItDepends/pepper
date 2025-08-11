(ns pepper.game.jobs
  ":unit-jobs --> a map of jobs indexed by unit id
   
   job --> either a map or nil
     REQUIRED KEYS
       :action --> fn of [game job] -> job

     POSSIBLE KEYS
       :completed? --> if true, the job will be set to nil"
  (:refer-clojure :exclude [type])
  (:require [pepper.game.job :as job])
  (:import [bwapi Game]))

(defn assign-unit-job [state job]
  (update-in state [:unit-jobs] assoc (:unit-id job) job))

(defn get-jobs-by-unit-id [state]
  (:unit-jobs state))

(defn get-unit-jobs [state]
  (vals (:unit-jobs state)))

(defn delete-job [state job]
  (update-in state [:unit-jobs] dissoc (:unit-id job)))

(defn with-frame-last-executed! [job game]
  (assoc job :frame-last-executed (Game/.getFrameCount game)))

(defn execute-job!
  "maybe it could be a multi step process ?
   1. get impure data
   2. get pure data from impure data
   3. decide with pure data
   4. do impure actions
   5. get pure data from impure actions
   6. decide
   
   maybe a job action could also have ?
   {:let {:unit [Game/.getUnit unit-id]
          :gathering? [Unit/.isGatheringMinerals :unit]}
   :args [:unit :gathering]
   :fn (fn [gathering?] blablabla)}"
  [job game]
  ((:action job) game job))

(defn filter-pending-jobs [jobs]
  (filterv (complement :run?) jobs))

(defn get-unit-job [state id]
  (get (get-jobs-by-unit-id state) id))

(defn process-job! [job game]
  (cond
    (nil? job) nil
    (job/completed? job) nil
    :else (-> (execute-job! job game)
              (with-frame-last-executed! game))))

(defn process-jobs!
  [game]
  (fn [jobs]
    (reduce-kv
     (fn [m id job]
       (assoc m id (process-job! job game)))
     {}
     jobs)))

(defn process-state-jobs! [state game]
  (update state :unit-jobs (process-jobs! game)))
