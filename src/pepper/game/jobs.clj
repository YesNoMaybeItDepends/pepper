(ns pepper.game.jobs)

(defn assign-unit-job [state job]
  (update-in state [:unit-jobs] assoc (:unit-id job) job))

(defn get-unit-jobs [state]
  (vals (:unit-jobs state)))

(defn delete-job [state job]
  (update-in state [:unit-jobs] dissoc (:unit-id job)))

(defn execute-job!
  ([game]
   (fn [job]
     (assoc job
            :result ((:action job) game job)
            :run? true))))

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

(def training-job {:train-unit! :training-unit?
                   :training-unit? :unit-completed?
                   :unit-completed? :unit-completed})