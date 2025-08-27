(ns pepper.bot.military
  (:require
   [pepper.bot.jobs.find-enemy-starting-base :as find-enemy-starting-base]
   [pepper.bot.macro :as macro]
   [pepper.bot.our :as our]
   [pepper.game :as game]
   [pepper.game.map :as map]
   [pepper.game.unit :as unit]))

(defn update-on-frame [[military messages] unit-jobs]
  #_(military/maybe-find-enemy-starting-base)
  [military (or messages [])])

(defn find-enemy-starting-base-jobs [unit-jobs]
  (->> unit-jobs
       (filter #(= (:job %) :find-enemy-starting-base))))

(defn already-scouting? [unit-jobs]
  (first (find-enemy-starting-base-jobs unit-jobs)))

(defn barracks-completed? [our game]
  (not-empty (->> (our/units our game)
                  ;; (filter #(unit/type? % :barracks))
                  (filter #(unit/type? % :supply-depot))
                  (filter :completed?))))

(defn enemy-starting-base [military]
  (:enemy-starting-base military))

(defn empty-starting-bases [military]
  (or (:empty-starting-bases military)
      []))

(defn fetch-our-starting-base [our game]
  (our/starting-base our game))

(defn fetch-all-starting-bases [game]
  (map/starting-bases (game/get-map game)))

(defn possible-enemy-starting-bases [military our game]
  (let [all-starting-bases (fetch-all-starting-bases game)
        our-starting-base (fetch-our-starting-base our game)
        empty-starting-bases (empty-starting-bases military)
        not-enemy-starting-bases (into #{} (conj empty-starting-bases our-starting-base))]
    (filterv (complement not-enemy-starting-bases) all-starting-bases)))

(defn starting-bases-already-being-scouted [unit-jobs]
  (mapv find-enemy-starting-base/starting-base-to-scout (find-enemy-starting-base-jobs unit-jobs)))

(defn know-enemy-starting-base? [military]
  (some? (enemy-starting-base military)))

(defn add-unit-finding-enemy-starting-base [military unit-id]
  (update military :units-finding-enemy-starting-base conj unit-id))

(defn update-military [military our game unit-jobs]
  military)

(defn military [state]
  (:military state))

(defn init-military [our game]
  {:military {:our-starting-base (fetch-our-starting-base our game)
              :enemy-starting-base nil
              :possible-enemy-starting-bases (filterv (complement #{fetch-our-starting-base}) (fetch-all-starting-bases game))
              :discarded-enemy-starting-bases []
              :units-finding-enemy-starting-base #{}}}) ;; unit -> base tuple, eg [3 [7 113]]

(defn assign-scouting-job [military worker-id our unit-jobs game]
  (let [possible-enemy-starting-bases (possible-enemy-starting-bases military our game)
        starting-bases-already-being-scouted (starting-bases-already-being-scouted unit-jobs)
        starting-base-to-scout (first possible-enemy-starting-bases) ;; TODO: filter starting-bases-already-being-scouted !!!!!!!
        job (find-enemy-starting-base/job starting-base-to-scout worker-id)] ;; TODO: if some base to scout then add job and stuff, otherwise skip adding it bro
    (-> military
        (add-unit-finding-enemy-starting-base worker-id)
        (update :job-updates (fn [updates] (into [] (concat ;; wtf 
                                                     (or updates
                                                         [])
                                                     (or job
                                                         []))))))))

(defn maybe-find-enemy-starting-base [military our unit-jobs game]
  (let [know-enemy-starting-base? (know-enemy-starting-base? military)
        already-scouting? (already-scouting? unit-jobs)
        barracks-completed? (barracks-completed? our game)
        some-available-worker (macro/get-idle-or-mining-worker)]
    (if (and (not know-enemy-starting-base?)
             barracks-completed?
             (not already-scouting?)
             some-available-worker)
      (assign-scouting-job military (unit/id some-available-worker) our unit-jobs game)
      military)))

;; (defn possible-enemy-base? [{:keys [possible-enemy-starting-bases]}])

;; (defn init-intel [state])

;; (defn init-military [state])
  