(ns pepper.game.jobs
  (:import [bwapi Game]))

(defn assign-unit-job [state job]
  (update-in state [:unit-jobs] assoc (:unit-id job) job))

(defn get-jobs-by-unit-id [state]
  (:unit-jobs state))

(defn get-unit-jobs [state]
  (vals (:unit-jobs state)))

(defn delete-job [state job]
  (update-in state [:unit-jobs] dissoc (:unit-id job)))

(defn execute-job!
  ([game]
   (fn [job]
     (assoc job
            :result ((:action job) game job)
            :run? true
            :frame-last-executed (Game/.getFrameCount game)))))

(defn dispatch-jobs! [game jobs]
  (mapv (execute-job! game) jobs))

(defn filter-pending-jobs [jobs]
  (filterv (complement :run?) jobs))

(defn process-jobs!
  "Jobs in general should probably be in their own namespace"
  [state game]
  (update
   state :unit-jobs
   #(update-vals % (fn [job]
                     (if (:run? job)
                       job
                       ((execute-job! game) job))))))

(defn get-unit-job [state id]
  (get (get-jobs-by-unit-id state) id))