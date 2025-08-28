(ns pepper.bot.macro
  (:require
   [pepper.bot.job :as job]
   [pepper.bot.unit-jobs :as unit-jobs]
   [pepper.bot.jobs.build :as build]
   [pepper.bot.jobs.train :as train]
   [pepper.bot.macro.auto-supply :as auto-supply]
   [pepper.bot.macro.mining :as mining]
   [pepper.bot.our :as our]
   [pepper.game :as game]
   [pepper.game.resources :as resources]
   [pepper.game.unit :as unit]
   [pepper.game.unit-type :as unit-type]))

(defn workers [our game]
  (->> (our/units our game)
       (filterv #(unit/type? % unit-type/worker))))

(defn mineral-fields [game]
  (->> (game/units game)
       (filterv #(unit/type? % unit-type/mineral-field))))

(defn idle-workers [our game]
  (->> (workers our game)
       (filterv unit/idle?)))

(defn mining-workers [our game unit-jobs]
  (let [mining-jobs-by-unit-id (set (->> unit-jobs
                                         (filterv #(job/type? % :mining))
                                         (mapv job/unit-id)))]
    (filter #(mining-jobs-by-unit-id (unit/id %)) (workers our game))))

(defn get-idle-or-mining-worker [our game unit-jobs] ;; TODO: iterate over unit-ids instead of units
  (or (first (idle-workers our game))
      (first (mining-workers our game unit-jobs))))

(defn maybe-build-barracks [[macro messages]]
  [macro []])

(defn maybe-build-supply [[macro messages] our game unit-jobs]
  (if (and (auto-supply/need-supply? our game)
           (auto-supply/can-afford? our game)
           (not (auto-supply/building-supply? (vals unit-jobs))))
    (let [worker (get-idle-or-mining-worker our game unit-jobs)
          new-job (build/job (unit/id worker) :supply-depot)]
      [macro [new-job]])
    [macro []]))

(defn maybe-train-units [[macro messages] our game unit-jobs]
  (let [trains {:command-center :scv
                :barracks :marine}
        trainers (->> (our/units our game)
                      (filterv #(unit/type? % (keys trains)))
                      (filterv unit/idle?)
                      (filterv #(not (some? (get unit-jobs (unit/id %))))))
        budget (our/resources-available our game)
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
    [macro (or new-training-jobs [])]))

(defn handle-idle-workers [[macro messages] our game]
  (let [idle-worker-ids (mapv :id (idle-workers our game))
        mineral-field-ids (mapv :id (mineral-fields game))
        new-mining-jobs (mining/new-mining-jobs idle-worker-ids mineral-field-ids)]
    [macro (or new-mining-jobs [])]))

(defn update-on-frame [[macro messages] our unit-jobs game]
  (let [[macro new-mining-jobs] (handle-idle-workers [macro messages] our game)
        [macro new-training-jobs] (maybe-train-units [macro messages] our game unit-jobs)
        [macro new-building-jobs] (maybe-build-supply [macro messages] our game unit-jobs)
        [macro new-build-rax-jobs] (maybe-build-barracks [macro messages])
        messages (into (or messages []) (concat new-mining-jobs new-training-jobs new-building-jobs new-build-rax-jobs))]
    [macro messages]))
