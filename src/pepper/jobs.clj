(ns pepper.jobs)

(def by-uuid (atom {}))
(defn as-list [] (vals @by-uuid))
(defn as-map [] @by-uuid)

(defn add!
  "Adds a job. If the job has no UUID, generates the UUID for it."
  [{uuid :uuid :as job}]
  (if (nil? uuid) (add! (assoc job :uuid (clojure.core/random-uuid)))
      (swap! by-uuid #(assoc % uuid job))))

(defn remove! [{uuid :uuid :as job}]
  (swap! by-uuid #(dissoc % uuid job)))

(defn update!
  "TODO: we assume job has a uuid, but it don't always be the case
   TODO: gets called A LOT"
  [{uuid :uuid :as job}]
  (when uuid  (swap! by-uuid #(assoc % uuid job))))

(defn find-job-by-building-id [])
(defn find-job-by-worker-id [])

(defn get-by-uuid
  [uuid]
  (get @by-uuid uuid))

(defn run-jobs! [game jobs]
  (doseq [job jobs]
    (let [job ((:run-fn job) game job)]
      (update! job))))