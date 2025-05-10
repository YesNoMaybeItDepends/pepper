(ns pepper.jobs.build
  (:require
   [pepper.api.game :as game]
   [pepper.api.player :as player]
   [pepper.api.unit :as unit]
   [pepper.api.unit-type :as unit-type]
   [pepper.jobs :as jobs]
   [pepper.jobs.gather :as gather]))

(defn building-in-job? [building-kw {building :building}]
  (= (:type building) (unit-type/kw->type building-kw)))

(defn building-in-jobs? [building-kw jobs]
  (some (fn [job] (building-in-job? building-kw job)) jobs))

(defn building-count [building-kw player]
  (player/all-unit-count player (unit-type/kw->type building-kw)))

(defn get-build-location [game unit-type player]
  (let [start-location (player/get-start-location player)]
    (game/get-build-location game unit-type start-location)))

(defn order-build [game unit-type unit]
  (let [player (game/self)
        worker unit
        location (get-build-location game unit-type player)]
    (unit/build worker unit-type location)))

(defn available-worker? [unit]
  (let [type (unit/get-type unit)
        is-worker (unit-type/is-worker? type)
        is-idle (unit/is-idle? unit)
        is-gathering-minerals (unit/is-gathering-minerals? unit)]
    (when (and is-worker (or is-idle is-gathering-minerals))
      unit)))

(defn find-available-worker [player]
  (first (filter available-worker? (player/get-units player))))

(defn run
  [game {building :building
         worker :worker
         :as job}]
  (cond
    (not (:id worker)) (let [unit (find-available-worker (game/self))
                             id (unit/get-unit-id unit)
                             type (:type building)]
                         (cond (game/can-make type unit)
                               (do (order-build game type unit)
                                   (assoc job :worker (assoc worker :id id)))
                               :else job))
    :else job))

(defn job [building-kw]
  (when (unit-type/valid-kw? building-kw)
    {:uuid (clojure.core/random-uuid)
     :building {:id nil :unit nil :type (unit-type/kw->type building-kw)}
     :worker {:id nil :unit nil}
     :run-fn #'run}))

(defn update! "TODO: overrides existing properties, should merge the maps"
  [{building :building worker :worker}]
  (let [job (first (filter #(= (:id worker) (get-in % [:worker :id])) (jobs/as-list)))
        job (assoc job :building building :worker worker)]
    (jobs/update! job)))

(defn handle-building-complete [{unit :unit}]
  (let [id (unit/get-unit-id unit)
        job (first (filter #(= id (get-in % [:building :id])) (jobs/as-list)))]
    (when job (jobs/update! (assoc job :run-fn #'gather/run)))))

(defn on-unit-create
  "Hooked to the on-unit-create game event"
  [{unit :unit}]
  (let [type (unit/get-type unit)]
    (when (unit-type/is-building? type)
      (let [building unit
            building-id (unit/get-unit-id unit)
            building-type (unit/get-type unit)
            worker (unit/get-build-unit unit)
            worker-id (unit/get-unit-id worker)
            worker-type (unit/get-type worker)]
        (update! {:building {:unit building
                             :id building-id
                             :type building-type}
                  :worker {:unit worker
                           :id worker-id
                           :type worker-type}})))))

(defn on-unit-complete [{game :game unit :unit}]
  (let [type (unit/get-type unit)
        is-building? (unit-type/is-building? type)]
    (when is-building? (handle-building-complete {:game game :unit unit :type type}))))