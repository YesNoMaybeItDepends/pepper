(ns pepper.bot.military
  (:require
   [pepper.bot.job :as job]
   [pepper.bot.jobs.find-enemy-starting-base :as find-enemy-starting-base]
   [pepper.bot.macro :as macro]
   [pepper.game.player :as player]
   [pepper.game.unit :as unit]
   [pepper.game.map :as game-map]
   [pepper.game.map.area :as area]))

(defn find-enemy-starting-base-jobs [unit-jobs]
  (filterv #(= (:job %) :find-enemy-starting-base) unit-jobs))

(defn already-scouting? [unit-jobs]
  (first (find-enemy-starting-base-jobs unit-jobs)))

(defn barracks-completed? [our-units]
  (not-empty (->> our-units
                  (filterv #(unit/type? % :barracks))
                  (filterv :completed?))))

(defn enemy-starting-base [military]
  (:enemy-starting-base military))

(defn empty-starting-bases [military]
  (or (:empty-starting-bases military)
      []))

(defn set-our-main-base-id [military base-id]
  (assoc military :our-main-base-id base-id))

(defn our-main-base-id [military]
  (:our-main-base-id military))

(defn set-their-main-base-by-id [military base-id]
  (assoc military :their-main-base-id base-id))

(defn their-main-base-id [military]
  (:their-main-base-id military))

(defn set-swarm-rally-point [military position]
  (assoc military :swarm-rally-position position))

(defn swarm-rally-position [military]
  (:swarm-rally-position military))

(defn possible-enemy-starting-bases [military our-player all-starting-bases]
  (let [our-starting-base (player/starting-base our-player)
        empty-starting-bases (empty-starting-bases military)
        not-enemy-starting-bases (into #{} (conj empty-starting-bases our-starting-base))]
    (filterv (complement not-enemy-starting-bases) all-starting-bases)))

(defn starting-bases-already-being-scouted [unit-jobs]
  (mapv find-enemy-starting-base/starting-base-to-scout (find-enemy-starting-base-jobs unit-jobs)))

(defn know-enemy-starting-base? [military]
  (some? (enemy-starting-base military)))

(defn set-unit-finding-enemy-starting-base [military worker]
  (update military :units-finding-enemy-starting-base conj (unit/id worker)))

(defn assign-scouting-job [military worker unit-jobs frame our-player starting-bases]
  (let [possible-enemy-starting-bases (possible-enemy-starting-bases military our-player starting-bases)
        starting-bases-already-being-scouted (starting-bases-already-being-scouted unit-jobs)
        starting-base-to-scout (first possible-enemy-starting-bases) ;; TODO: filter starting-bases-already-being-scouted !!!!!!!
        job (job/init (find-enemy-starting-base/job starting-base-to-scout (unit/id worker)) frame)
        military (set-unit-finding-enemy-starting-base military worker)] ;; TODO: if some base to scout then add job and stuff, otherwise skip adding it bro
    [military [job]]))

(defn assign-scouting-job? [know-enemy-starting-base?
                            barracks-completed?
                            already-scouting?
                            some-available-worker]
  (and
   (not know-enemy-starting-base?)
   barracks-completed?
   (not already-scouting?)
   some-available-worker))

(defn maybe-find-enemy-starting-base [[military messages] our-units our-player unit-jobs frame starting-bases]
  (let [know-enemy-starting-base? (know-enemy-starting-base? military)
        barracks-completed? (barracks-completed? our-units)
        already-scouting? (already-scouting? unit-jobs)
        some-available-worker (macro/get-idle-or-mining-worker (macro/workers our-units) unit-jobs)
        assign-job? (assign-scouting-job? know-enemy-starting-base?
                                          barracks-completed?
                                          already-scouting?
                                          some-available-worker)
        [military jobs] (if assign-job?
                          (assign-scouting-job military some-available-worker unit-jobs frame our-player starting-bases)
                          [military []])]
    [military jobs]))

(defn get-our-main-base-geography [our-main-base-id game-map]
  (let [base (game-map/get-base-by-id game-map our-main-base-id)
        area (game-map/get-base-area base game-map)
        choke-points (game-map/get-area-choke-points area game-map)]
    {:base base
     :area area
     :choke-points choke-points}))

(defn determine-rally-point [our-main-base-id game-map]
  (let [{:keys [base area choke-points]} (get-our-main-base-geography our-main-base-id game-map)
        accessible-neighbors (game-map/get-area-accessible-neighbors area game-map)
        choke-points-to-accessible-neighbors (area/choke-points-to-accessible-neighbors area accessible-neighbors choke-points)]
    (mapv :center choke-points-to-accessible-neighbors)))

(defn pair-colls-randomly [coll-to-pair coll-with]
  (mapv #(vector % (rand-nth coll-with)) coll-to-pair))

;;;;

(defn maybe-rally-marines [[military messages] our-units our-player unit-jobs frame starting-bases]
  [military []])

;;;;

(defn our-units [units player] ;; where do i put this its everywhere
  (filterv #(unit/owned-by-player? % player) units))

;;;; 

(defn init-military [our-player starting-bases]
  (let [our-starting-base (player/starting-base our-player)]
    {:our-starting-base our-starting-base
     :enemy-starting-base nil
     :possible-enemy-starting-bases (filterv (fn [base] (nil? (#{our-starting-base} base))) starting-bases)
     :discarded-enemy-starting-bases []
     :units-finding-enemy-starting-base #{}})) ;; unit -> base tuple, eg [3 [7 113]]

(defn update-on-start [our-player starting-bases]
  (init-military our-player starting-bases))

(defn update-on-frame [[military messages] {:keys [units players frame starting-bases unit-jobs]}]
  (let [our-player (player/our-player players)
        our-units (our-units units our-player)
        [military new-scouting-jobs] (maybe-find-enemy-starting-base [military messages] our-units our-player unit-jobs frame starting-bases)
        [military new-rally-jobs] (maybe-rally-marines [military messages] our-units our-player unit-jobs frame starting-bases)]
    [military (into messages conj (concat (or new-scouting-jobs []) (or new-rally-jobs [])))]))