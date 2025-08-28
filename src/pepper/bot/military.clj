(ns pepper.bot.military
  (:require
   [pepper.bot.jobs.find-enemy-starting-base :as find-enemy-starting-base]
   [pepper.bot.macro :as macro]
   [pepper.bot.our :as our]
   [pepper.game :as game]
   [pepper.game.map :as map]
   [pepper.game.unit :as unit]))

(defn find-enemy-starting-base-jobs [unit-jobs]
  (filterv #(= (:job %) :find-enemy-starting-base) unit-jobs))

(defn already-scouting? [unit-jobs]
  (first (find-enemy-starting-base-jobs unit-jobs)))

(defn barracks-completed? [our game]
  (not-empty (->> (our/units our game)
                  (filterv #(unit/type? % :barracks))
                  (filterv :completed?))))

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

(defn military [state]
  (:military state))

(defn init-military [our game]
  {:our-starting-base (fetch-our-starting-base our game)
   :enemy-starting-base nil
   :possible-enemy-starting-bases (filterv (complement #{fetch-our-starting-base}) (fetch-all-starting-bases game))
   :discarded-enemy-starting-bases []
   :units-finding-enemy-starting-base #{}}) ;; unit -> base tuple, eg [3 [7 113]]

(defn assign-scouting-job [military worker-id our unit-jobs game]
  (let [possible-enemy-starting-bases (possible-enemy-starting-bases military our game)
        starting-bases-already-being-scouted (starting-bases-already-being-scouted unit-jobs)
        starting-base-to-scout (first possible-enemy-starting-bases) ;; TODO: filter starting-bases-already-being-scouted !!!!!!!
        job (find-enemy-starting-base/job starting-base-to-scout worker-id)
        military (add-unit-finding-enemy-starting-base military worker-id)] ;; TODO: if some base to scout then add job and stuff, otherwise skip adding it bro
    [military [job]]))

(defn assign-scouting-job? [know-enemy-starting-base?
                            barracks-completed?
                            already-scouting?
                            some-available-worker]
  (and (not know-enemy-starting-base?)
       barracks-completed?
       (not already-scouting?)
       some-available-worker))

(defn maybe-find-enemy-starting-base [military our unit-jobs game]
  (let [know-enemy-starting-base? (know-enemy-starting-base? military)
        barracks-completed? (barracks-completed? our game)
        already-scouting? (already-scouting? unit-jobs)
        some-available-worker (macro/get-idle-or-mining-worker our game unit-jobs)
        assign-job? (assign-scouting-job? know-enemy-starting-base?
                                          barracks-completed?
                                          already-scouting?
                                          some-available-worker)
        [military jobs] (if assign-job?
                          (assign-scouting-job military (unit/id some-available-worker) our unit-jobs game)
                          [military []])]
    [military jobs]))

(defn update-military [military our game unit-jobs]
  military)

(defn update-on-start [our game]
  (init-military our game))

(defn update-on-frame [[military messages] our unit-jobs game]
  (let [[military new-jobs] (maybe-find-enemy-starting-base military our (vals unit-jobs) game)]
    [military (into messages conj (or new-jobs []))]))

;; (defn possible-enemy-base? [{:keys [possible-enemy-starting-bases]}])

;; (defn init-intel [state])

;; (defn init-military [state])
  