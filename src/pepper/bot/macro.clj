(ns pepper.bot.macro
  (:require
   [pepper.bot.jobs :as jobs]
   [pepper.bot.jobs.gather :as gather]
   [pepper.bot.our :as our]
   [pepper.game :as game]
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

(defn assign-random-mineral
  [mineral-fields]
  (fn [worker]
    [worker (rand-nth mineral-fields)]))

(defn process-idle-workers [macro our game]
  (let [idle-workers (map :id (idle-workers our game))
        mineral-fields (mineral-fields game)
        new-jobs (->> (map (assign-random-mineral mineral-fields)
                           idle-workers)
                      (map gather/mining-job))]
    (reduce jobs/assign-unit-job {} new-jobs)))

(defn update-on-frame [macro]
  #_(macro/process-macro)
  macro)