(ns pepper.bot.macro.mining
  (:require
   [pepper.bot.job :as job]
   [pepper.bot.jobs.gather :as gather]))

(defn assign-random-mineral
  [mineral-fields]
  (fn [worker]
    [worker (rand-nth mineral-fields)]))

(defn new-mining-jobs [idle-worker-ids mineral-field-ids frame]
  (if (and (not-empty idle-worker-ids)
           (not-empty mineral-field-ids))
    (let [new-mining-jobs (->> (mapv (assign-random-mineral mineral-field-ids)
                                     idle-worker-ids)
                               (mapv gather/gather-job)
                               (mapv #(job/init % frame)))]
      new-mining-jobs)
    []))