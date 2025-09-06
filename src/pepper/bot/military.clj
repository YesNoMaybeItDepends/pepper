(ns pepper.bot.military
  (:require
   [pepper.bot.job :as job]
   [pepper.bot.jobs.attack-move :as attack-move]
   [pepper.bot.jobs.find-enemy-starting-base :as find-enemy-starting-base]
   [pepper.bot.macro :as macro]
   [pepper.game.map :as game-map]
   [pepper.game.map.area :as area]
   [pepper.game.player :as player]
   [pepper.game.unit :as unit]
   [pepper.game.unit-type :as unit-type]))

(defn find-enemy-starting-base-jobs [unit-jobs]
  (filterv #(= (:job %) :find-enemy-starting-base) unit-jobs))

(defn already-scouting? [unit-jobs]
  (first (find-enemy-starting-base-jobs unit-jobs)))

(defn barracks-completed? [our-units]
  (not-empty (->> our-units
                  (filterv (unit/type? :barracks))
                  (filterv :completed?))))

(defn set-enemy-starting-base [military base-id]
  (assoc military :enemy-starting-base base-id))

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

(defn player-units [units player] ;; where do i put this its everywhere
  (filterv #(unit/owned-by-player? % player) units))

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

(defn see-enemy-main? [units possible-enemy-starting-bases]
  (first (->> units
              (filterv (unit/type? unit-type/town-hall))
              (filterv (fn [u] (some #(= % (:tile u)) possible-enemy-starting-bases)))
              (filterv (comp not unit/dead?))
              (mapv :tile))))

(defn maybe-try-find-enemy-starting-base [[military messages] units our-player unit-jobs frame starting-bases]
  (let [our-units (player-units units our-player)
        barracks-completed? (barracks-completed? our-units)
        already-scouting? (already-scouting? unit-jobs)
        some-available-worker (macro/get-idle-or-mining-worker (macro/workers our-units) unit-jobs)
        assign-job? (and barracks-completed?
                         (not already-scouting?)
                         some-available-worker)
        [military jobs] (if assign-job?
                          (assign-scouting-job military some-available-worker unit-jobs frame our-player starting-bases)
                          [military []])]
    [military jobs]))

(defn maybe-find-enemy-starting-base [[military messages] units our-player unit-jobs frame starting-bases]
  (let [know-enemy-starting-base? (know-enemy-starting-base? military)
        see-enemy-main? (see-enemy-main? units (:possible-enemy-starting-bases military))]
    (cond
      know-enemy-starting-base?
      [military []]

      (and (not know-enemy-starting-base?) see-enemy-main?)
      [(set-enemy-starting-base military see-enemy-main?) []]

      (and (not know-enemy-starting-base?) (not see-enemy-main?))
      (maybe-try-find-enemy-starting-base
       [military messages] units our-player unit-jobs frame starting-bases)

      :else
      [military []])))

(defn get-main-geography [base-id game-map]
  (let [base (game-map/get-base-by-id game-map base-id)
        area (game-map/get-base-area base game-map)
        choke-points (game-map/get-area-choke-points area game-map)]
    {:base base
     :area area
     :choke-points choke-points}))

(defn get-main-ramp
  "TODO: There could be more than 1 ramp, but right now I'm always getting the first one"
  [base-id game-map]
  (let [{:keys [base area choke-points]} (get-main-geography base-id game-map)
        accessible-neighbors (game-map/get-area-accessible-neighbors area game-map)
        choke-points-to-accessible-neighbors (area/choke-points-to-accessible-neighbors area accessible-neighbors choke-points)]
    (first (mapv :center choke-points-to-accessible-neighbors))))

(defn get-main-natural
  "TODO: There could be more than 1 natural, but right now I'm always getting the first one"
  [base-id game-map]
  (let [{:keys [base area choke-points]} (get-main-geography base-id game-map)
        accessible-neighbors (game-map/get-area-accessible-neighbors area game-map)
        base-ids (mapv game-map/get-area-base-ids accessible-neighbors)
        bases (mapv #(game-map/get-base-by-id game-map %) base-ids)]
    (first bases)))

(defn enough-units-to-move-out? [units]
  (< 50 (count units)))

(defn units-that-can-attack [our-units]
  (transduce
   (comp
    (filter (complement unit/dead?))
    (filter (unit/type? :marine)))
   conj []
   our-units))

(defn ready-to-move-out? [our-units]
  (-> our-units
      units-that-can-attack
      enough-units-to-move-out?))

(defn pair-colls-randomly [coll-to-pair coll-with]
  (mapv #(vector % (rand-nth coll-with)) coll-to-pair))

;;;;

(defn maybe-rally-marines [[military messages] game-map players units unit-jobs frame]
  (let [our-units (filterv :completed? (player-units units (player/our-player players)))
        their-units (player-units units (player/enemy-player players))
        units-to-kill (filterv #(not (unit/dead? %)) their-units)
        buildings-to-kill (filterv #(unit-type/building? (unit/type %)) units-to-kill)
        units-that-can-attack (units-that-can-attack our-units)
        their-main (enemy-starting-base military)
        their-ramp (get-main-ramp their-main game-map)
        our-main   (player/starting-base (player/our-player players))
        our-ramp (get-main-ramp our-main game-map)
        rally-point (if (or (enough-units-to-move-out? units-that-can-attack)
                            (not-empty (transduce
                                        (comp
                                         (filter (job/type? :attack-move))
                                         (filter (comp (conj #{} their-ramp their-main)
                                                       attack-move/target-position)))
                                        conj []
                                        unit-jobs)))
                      (if (some #(#{their-main} (unit/tile %)) buildings-to-kill)
                        their-main
                        (:tile (first buildings-to-kill)))
                      our-ramp)
        units-to-rally-xf (comp (filter (complement :attack-frame?))
                                (filter (complement :starting-attack?)))
        units-to-rally (transduce units-to-rally-xf conj [] units-that-can-attack)
        new-jobs (mapv #(job/init (attack-move/job (unit/id %) rally-point) frame) units-to-rally)]
    [military new-jobs]))

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

(defn update-on-frame [[military messages] {:keys [units players frame game-map unit-jobs]}]
  (let [our-player (player/our-player players)
        starting-bases (game-map/starting-bases game-map)
        [military new-scouting-jobs] (maybe-find-enemy-starting-base [military messages] units our-player unit-jobs frame starting-bases)
        [military new-rally-jobs] (maybe-rally-marines [military messages] game-map players units unit-jobs frame)]
    [military (into messages conj (concat (or new-scouting-jobs []) (or new-rally-jobs [])))]))