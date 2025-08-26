(ns pepper.bot.macro.mining
  (:require
   [pepper.bot.jobs.gather :as gather]))

(defn assign-random-mineral
  [mineral-fields]
  (fn [worker]
    [worker (rand-nth mineral-fields)]))

(defn new-mining-jobs [idle-worker-ids mineral-field-ids]
  (let [new-mining-jobs (->> (map (assign-random-mineral mineral-field-ids)
                                  idle-worker-ids)
                             (map gather/mining-job))]
    new-mining-jobs))