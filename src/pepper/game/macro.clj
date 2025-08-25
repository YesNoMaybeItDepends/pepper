(ns pepper.game.macro
  (:require
   [pepper.game.auto-supply :as auto-supply]
   [pepper.game.jobs :as jobs]
   [pepper.bot.jobs.build :as build]
   [pepper.bot.jobs.train :as train]
   [pepper.game.resources :as resources]
   [pepper.game.unit :as unit]
   [pepper.game.unit-type :as unit-type]
   [pepper.bot.job :as job]))

(defn already-building? [building state]
  (->> (jobs/get-unit-jobs state)
       (filter #(= (build/building %) building))
       first))

(defn maybe-build-supply [state]
  (if (and (auto-supply/need-supply? state)
           (auto-supply/can-afford? state)
           (not (auto-supply/building-supply? state)))
    (let [worker (unit/get-idle-or-mining-worker state)]
      (jobs/assign-unit-job state (build/job (unit/id worker) :supply-depot)))
    state))

(defn maybe-build-barracks [state]
  (let [budget (resources/get-state-resources-available state)
        cost (unit-type/cost :barracks)
        can-afford? (resources/can-afford?-v2 budget cost)
        some-worker (unit/get-idle-or-mining-worker state)
        already-building? (already-building? :barracks state)]
    (if (and (not already-building?) some-worker can-afford?)
      (jobs/assign-unit-job state (build/job (unit/id some-worker) :barracks))
      state)))

(defn maybe-train-units [state]
  (let [trains {:command-center :scv
                :barracks :marine}
        costs {:scv (unit-type/cost :scv)
               :marine (unit-type/cost :marine)}
        trainers (->> (unit/get-our-units state)
                      (filter #(unit/type? % (keys trains)))
                      (filter unit/idle?)
                      (filter #(unit/unemployed? state %)))
        budget (resources/get-state-resources-available state)
        [remaining-budget new-training-jobs] (reduce
                                              (fn [[budget jobs] trainer]
                                                (let [to-train ((unit/type trainer) trains)
                                                      to-pay (unit-type/cost to-train)]
                                                  (if (resources/can-afford?-v2 budget to-pay)
                                                    [(resources/combine-quantities - budget to-pay)
                                                     (conj jobs (-> (train/job (unit/id trainer) to-train)
                                                                    job/new))]
                                                    (reduced [budget jobs]))))
                                              [budget []]
                                              trainers)]
    (reduce jobs/assign-unit-job state new-training-jobs)))

(defn process-macro [state]
  (-> state
      resources/process-resources
      ;; gathering/process-idle-workers
      maybe-build-supply
      maybe-build-barracks
      maybe-train-units))