(ns pepper.game.jobs
  ":unit-jobs --> a map of jobs indexed by unit id
   
   job --> either a map or nil
     REQUIRED KEYS
       :action --> fn of [game job] -> job

     POSSIBLE KEYS
       :completed? --> if true, the job will be set to nil"
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

(defn execute-job! [job game]
  ((:action job) game job))

(defn filter-pending-jobs [jobs]
  (filterv (complement :run?) jobs))

(defn mark-job-completed [job]
  (assoc job :completed? true))

(defn job-completed? [job]
  (:completed? job))

(defn process-completed-job [job]
  nil)

(defn get-unit-job [state id]
  (get (get-jobs-by-unit-id state) id))

(defn process-job! [job game]
  (cond
    (nil? job) nil
    (job-completed? job) nil
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

(defn validate-job [job]
  (assert (:action job) "job requires an action")
  job)