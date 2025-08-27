(ns pepper.bot.macro
  (:require
   [pepper.bot.jobs.build :as build]
   [pepper.bot.macro.auto-supply :as auto-supply]
   [pepper.bot.macro.mining :as mining]
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

;; (defn group-workers-by-job [workers jobs]
;; (->> (group-by #(job/type (jobs/get-unit-job state (id %))))))

(defn get-idle-or-mining-worker [])

;; (defn get-idle-or-mining-worker [our game]
;;   (let [workers (workers our game)
;;         #_idle-workers #_(idle-workers our game)
;;         worker-jobs (unit-jobs/units->jobs {} workers)
;;         worker-jobs-by-job-type (unit-jobs/group-jobs-by-type worker-jobs)
;;         workers-by-job (group-workers-by-job state)
;;         worker-id (cond (some? (get workers-by-job nil))
;;                         (:id (first (get workers-by-job nil)))

;;                         (some? (:mining workers-by-job))
;;                         (:id (first (:mining workers-by-job))))]
;;     (get-unit-by-id state worker-id)))

(defn maybe-build-supply [macro our game unit-jobs]
  (if (and (auto-supply/need-supply? our game)
           (auto-supply/can-afford? our game)
           (not (auto-supply/building-supply? unit-jobs)))
    (let [worker (get-idle-or-mining-worker)]
      (update macro :job-updates into (build/job (unit/id worker) :supply-depot)))
    macro))

(defn maybe-build-barracks [macro]
  macro)

(defn maybe-train-units [macro]
  macro)

(defn update-on-frame [[macro messages] our unit-jobs game]
  (let [idle-worker-ids (mapv :id (idle-workers our game))
        mineral-field-ids (mapv :id (mineral-fields game))
        new-mining-jobs (mining/new-mining-jobs idle-worker-ids mineral-field-ids)
        macro (-> macro
                  maybe-build-barracks
                  maybe-train-units)
        messages (into (or messages []) new-mining-jobs)]
    [macro messages]
    #_(-> macro
          #_(process-idle-workers our game)
          #_(maybe-build-supply our game unit-jobs)
          (maybe-build-barracks)
          (maybe-train-units)
          (assoc :job-updates new-mining-jobs)
          #_(update :job-updates into (into [] new-mining-jobs))
          #_(update :job-updates into (into new-jobs new-mining-jobs)))))

