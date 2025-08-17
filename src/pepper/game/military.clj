(ns pepper.game.military
  (:require
   [pepper.game.jobs :as jobs]
   [pepper.game.map :as map]
   [pepper.game.player :as player]
   [pepper.game.jobs.find-enemy-starting-base :as find-enemy-starting-base]
   [pepper.game.unit :as unit]))

(defn find-enemy-starting-base-jobs [state]
  (->> (jobs/get-unit-jobs state)
       (filter #(= (:job %) :find-enemy-starting-base))))

(defn already-scouting? [state]
  (first (find-enemy-starting-base-jobs state)))

(defn barracks-completed? [state]
  (not-empty (->> (unit/get-our-units state)
                  ;; (filter #(unit/type? % :barracks))
                  (filter #(unit/type? % :supply-depot))
                  (filter :completed?))))

(defn enemy-starting-base [state]
  (:enemy-starting-base state))

(defn empty-starting-bases [state]
  (or (:empty-starting-bases state)
      []))

(defn our-starting-base [state]
  (player/starting-base (player/get-self state)))

(defn all-starting-bases [state]
  (map/starting-bases (map/map state)))

(defn possible-enemy-starting-bases [state]
  (let [all-starting-bases (all-starting-bases state)
        our-starting-base (our-starting-base state)
        empty-starting-bases (empty-starting-bases state)
        not-enemy-starting-bases (into #{} (conj empty-starting-bases our-starting-base))]
    (filterv (complement not-enemy-starting-bases) all-starting-bases)))

(defn starting-bases-already-being-scouted [state]
  (mapv find-enemy-starting-base/starting-base-to-scout (find-enemy-starting-base-jobs state)))

(defn know-enemy-starting-base? [state]
  (some? (enemy-starting-base state)))

(defn assign-scouting-job [state worker-id]
  (let [possible-enemy-starting-bases (possible-enemy-starting-bases state)
        starting-bases-already-being-scouted (starting-bases-already-being-scouted state)
        starting-base-to-scout (first possible-enemy-starting-bases) ;; TODO: filter starting-bases-already-being-scouted !!!!!!!
        job (find-enemy-starting-base/job starting-base-to-scout worker-id)] ;; TODO: if some base to scout then add job and stuff, otherwise skip adding it bro
    (jobs/assign-unit-job state job)))

(defn maybe-scout [state]
  (let [already-scouting? (already-scouting? state)
        barracks-completed? (barracks-completed? state)
        know-enemy-starting-base? (know-enemy-starting-base? state)
        some-available-worker (unit/get-idle-or-mining-worker state)]
    (if (and (not know-enemy-starting-base?)
             barracks-completed?
             (not already-scouting?)
             some-available-worker)
      (assign-scouting-job state (unit/id some-available-worker))
      state)))

;;     {:all-starting-bases all-starting-bases
;;      :our-starting-base our-starting-base
;;      :possible-enemy-starting-bases (filterv (complement #{our-starting-base}) all-starting-bases)
;;      :empty-starting-bases #{}
;;      :bases-being-checked #{}
;;      :bases-checked #{}
;;      :enemy-starting-base nil}

;; (defn possible-enemy-base? [{:keys [possible-enemy-starting-bases]}])

;; (defn init-intel [state])

;; (defn init-military [state])
  