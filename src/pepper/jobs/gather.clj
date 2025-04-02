(ns pepper.jobs.gather
  (:require
   [pepper.bwapi.game :as game]
   [pepper.bwapi.unit :as unit]
   [pepper.bwapi.unit-type :as unit-type]
   [pepper.jobs :as jobs]))

(defn get-closest-mineral
  [game unit]
  (:mineral (apply min-key :distance
                   (map (fn [mineral]
                          {:mineral mineral :distance (unit/get-distance unit mineral)})
                        (game/get-minerals game)))))

(defn handle-worker-complete [game unit]
  (when-some [closest-mineral (get-closest-mineral game unit)]
    (unit/gather unit closest-mineral)))

(defn on-unit-complete [{game :game unit :unit :as args}]
  (let [type (unit/get-type unit)
        is-worker? (unit-type/is-worker? type)]
    (when is-worker? (handle-worker-complete game unit))))

(defn run [game {worker :worker :as job}]
  (if (unit/is-gathering-minerals? (:unit worker))
    (jobs/remove! job)
    (handle-worker-complete game (:unit worker))))