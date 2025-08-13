(ns pepper.game.training
  (:require
   [pepper.game.jobs :as jobs]
   [pepper.game.jobs.train :as train]
   [pepper.game.resources :as resources]
   [pepper.game.unit :as unit]
   [pepper.game.unit-type :as unit-type])
  (:import
   [bwapi UnitType]))

(defn get-command-centers [state]
  (->> (unit/get-units state)
       (filter #(unit/ours? state %))
       (filter #(unit/type? % unit-type/town-hall))))

(defn get-idle-command-centers [state]
  (->> (get-command-centers state)
       (filter unit/idle?)
       (filter #(unit/unemployed? state %))))

(defn process-idle-command-centers [state]
  (let [idle-command-centers (mapv :id (get-idle-command-centers state))
        scv-cost (unit-type/cost :scv)
        jobs-to-update (reduce-kv
                        (fn [acc idx curr]
                          (if (resources/can-afford? state (-> scv-cost
                                                               (resources/multiply-quantity idx)
                                                               (resources/sum-quantities scv-cost)))
                            (conj acc (train/train-scv-job curr))
                            (reduced acc)))
                        []
                        idle-command-centers)]
    (reduce jobs/assign-unit-job state jobs-to-update)))