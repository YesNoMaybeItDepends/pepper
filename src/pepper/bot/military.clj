(ns pepper.bot.military
  (:require
   [pepper.bot.job :as job]
   [pepper.bot.jobs.attack-move :as attack-move]
   [pepper.bot.jobs.find-enemy-starting-base :as find-enemy-starting-base]
   [pepper.bot.macro :as macro]
   [pepper.game :as game]
   [pepper.game.map :as game-map]
   [pepper.game.map.area :as area]
   [pepper.game.player :as player]
   [pepper.game.position :as position]
   [pepper.game.unit :as unit]
   [pepper.game.unit-type :as unit-type]))

(defn ->event [name frame]
  {:event/name name
   :event/frame frame})

(defn event-name [event]
  (:event/name event))

(defn event-frame [event]
  (:event/frame event))

(defn valid-event? [event]
  (every? event [:event/name :event/frame]))

(def base-events
  {:scouted-base "We scouted a base"
   :confirmed-enemy-base "We are certain that the enemy controls a base"
   :confirmed-enemy-starting-base "We are certain that this is the enemy starting base"})

(defn valid-base-event? [base-event]
  (contains? base-events (:event/name base-event)))

(defn validate-base-event [base-event]
  (if-not (valid-base-event? base-event)
    (throw (Exception. "invalid base event"))
    base-event))

(defn ->base-event [name frame base]
  (assoc (->event name frame)
         :base-id base))

(defn events [military]
  (:events military []))

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

(defn enemy-starting-base-id [military] ;; used
  (:base-id (->> (events military)
                 (filterv (comp #{:confirmed-enemy-starting-base} event-name))
                 first)))

(defn empty-starting-bases [military]
  (or (:empty-starting-bases military)
      []))

(defn set-our-main-base-id [military base-id]
  (assoc military :our-main-base-id base-id))

(defn our-main-base-id [military]
  (:our-main-base-id military))

(defn frame-last-rallied [military]
  (:frame-last-rallied military))

(defn set-frame-last-rallied [military frame]
  (assoc military :frame-last-rallied frame))

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
  (some? (enemy-starting-base-id military)))

(defn set-unit-finding-enemy-starting-base [military worker]
  (update military :units-finding-enemy-starting-base conj (unit/id worker)))

(defn assign-scouting-job [military worker unit-jobs frame our-player starting-bases]
  (let [possible-enemy-starting-bases (possible-enemy-starting-bases military our-player starting-bases)
        starting-bases-already-being-scouted (starting-bases-already-being-scouted unit-jobs)
        starting-base-to-scout (first possible-enemy-starting-bases) ;; TODO: filter starting-bases-already-being-scouted !!!!!!!
        job (job/init (find-enemy-starting-base/job starting-base-to-scout (unit/id worker)) frame)
        military (set-unit-finding-enemy-starting-base military worker)] ;; TODO: if some base to scout then add job and stuff, otherwise skip adding it bro
    [military [job]]))

(defn see-enemy-main?
  "Do we see an enemy town hall in any of the starting bases?
   
   Returns a tile lol lmao"
  [units possible-enemy-starting-bases]
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

(defn base->areas [])

(defn unit->areas [unit map]
  (game-map/position->areas map (unit/position unit)))

(defn group-units-by-area [map units]
  (reduce
   (fn [acc unit]
     (reduce #(update-in %1 [%2] conj unit) acc (unit->areas unit map)))
   {}
   units))

(defn discover-enemy-bases [game]
  (let [all-bases (game-map/bases (game/get-map game))
        visible-enemy-units (filterv (every-pred unit/exists? unit/visible?) (game/enemy-units game))
        group-units-by-area (group-by (fn [x]
                                        :position) visible-enemy-units)]))

(defn update-intel [[military messages] game unit-jobs]
  (let [bases (game-map/bases (game/get-map game))
        areas (game-map/areas (game/get-map game))
        enemy-units (game/enemy-units game)]))

(defn maybe-find-enemy-starting-base [[military messages] game unit-jobs]
  (let [starting-bases (game-map/starting-bases (game/get-map game)) ;; as keyval
        units (game/units game)
        our-player (game/our-player game)
        frame (game/frame game)
        know-enemy-starting-base? (know-enemy-starting-base? military)
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

(defn maybe-find-enemy-starting-base-v2 [[military messages] game unit-jobs]
  (let [events (events military)
        enemy-starting-base-id (enemy-starting-base-id military)]
    enemy-starting-base-id))

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
    (filterv (comp nil? :blocking-neutral) choke-points-to-accessible-neighbors)))

(defn get-main-natural
  "TODO: There could be more than 1 natural, but right now I'm always getting the first one"
  [base-id game-map]
  (let [{:keys [base area choke-points]} (get-main-geography base-id game-map)
        accessible-neighbors (game-map/get-area-accessible-neighbors area game-map)
        base-ids (mapv game-map/get-area-base-ids accessible-neighbors)
        bases (mapv #(game-map/get-base-by-id game-map %) base-ids)]
    (first bases)))

(defn enough-units-to-move-out? [units]
  (< 45 (count (filterv (unit/type? #{:marine #_:firebat #_:medic}) units))))

(defn enough-abilities-to-move-out? [abilities]
  (let [must-have [:stim-packs]]
    (every? #(some #{%} abilities) must-have)))

(defn enough-upgrades-to-move-out [has-upgraded]
  (let [must-have [[:u-238-shells 1] [:terran-infantry-armor 1] [:terran-infantry-weapons 1]]]
    (every? #(some #{%} has-upgraded) must-have)))

(defn units-that-can-attack [our-units]
  (transduce
   (comp
    (filter (complement unit/dead?))
    (filter (unit/type? #{:marine :medic :firebat})))
   conj []
   our-units))

(defn ready-to-move-out? [our-units]
  (-> our-units
      units-that-can-attack
      enough-units-to-move-out?))

(defn pair-colls-randomly [coll-to-pair coll-with]
  (mapv #(vector % (rand-nth coll-with)) coll-to-pair))

;;;;

(defn slightly-behind-our-ramp [ramp main]
  (let [main (position/_->walk-position main)
        [x1 y1] [(:x main) (:y main)]
        [x2 y2] [(:x ramp) (:y ramp)]
        [w h] [(- x2 x1) (- y2 y1)] #_(if (< (+ x1 y1) (+ x2 y2)) ;; do I need this?
                                        [(- x2 x1) (- y2 y1)]
                                        [(- x1 x2) (- y1 y2)])]
    (position/->map (+ x1 (quot w 1.25) #_(quot w 2))
                    (+ y1 (quot h 1.25) #_(quot h 2))
                    :walk-position)))

(defn maybe-rally-marines [[military messages] game-map players units unit-jobs frame]
  (let [our-player (player/our-player players)
        our-units (filterv unit/completed? (player-units units our-player))
        their-units (player-units units (player/enemy-player players))
        units-to-kill (filterv #(not (unit/dead? %)) their-units)
        lurkers-to-kill (filterv (every-pred :visible? (complement :burrowed?) (unit/type? :lurker)) units-to-kill)
        buildings-to-kill (filterv #(unit-type/building? (unit/type %)) units-to-kill)
        units-that-can-attack (units-that-can-attack our-units)
        their-main (enemy-starting-base-id military)
        their-ramp (:center (first (get-main-ramp their-main game-map)))
        our-main   (player/starting-base our-player)
        our-ramp (:center (first (get-main-ramp our-main game-map)))
        slightly-behind-our-ramp (slightly-behind-our-ramp our-ramp our-main)
        our-ramp slightly-behind-our-ramp
        rally-point (if (or (and (enough-units-to-move-out? units-that-can-attack)
                                 (enough-abilities-to-move-out? (player/has-researched our-player))
                                 (enough-upgrades-to-move-out (player/has-upgraded our-player)))
                            (not-empty (transduce
                                        (comp
                                         (filter (job/type? :attack-move))
                                         (filter (comp (conj #{} their-ramp their-main)
                                                       attack-move/target-position)))
                                        conj []
                                        unit-jobs)))
                      (if (some #(#{their-main} (unit/tile %)) buildings-to-kill)
                        their-main
                        (if (some? (first buildings-to-kill))
                          (:tile (first buildings-to-kill))
                          (rand-nth (keys (game-map/bases-by-id game-map)))))
                      our-ramp)
        units-to-rally-xf (comp (filter (complement :attack-frame?))
                                (filter (complement :starting-attack?))
                                (filter (complement :under-attack?))
                                (filter (complement :stimmed?))
                                (if (= rally-point our-ramp)
                                  (filter (complement (unit/type? :scv)))
                                  (filter any?)))
        units-to-rally (transduce units-to-rally-xf conj [] units-that-can-attack)
        new-jobs (mapv #(job/init (attack-move/job (unit/id %) rally-point {:target-unit-id (unit/id (first lurkers-to-kill))}) frame) units-to-rally)
        frame-last-rallied (frame-last-rallied military)
        rally? (< (+ 250 (or frame-last-rallied 0)) frame)]
    [(if rally?
       (set-frame-last-rallied military frame)
       military)
     (if rally?
       new-jobs
       [])]))

;;;; 

(defn init-military
  "delete this probably"
  [our-player starting-bases]
  {})

(defn update-on-start [our-player starting-bases]
  (init-military our-player starting-bases))

(defn update-on-frame [[military messages] game unit-jobs]
  (let [[military new-scouting-jobs] (maybe-find-enemy-starting-base [military messages] game unit-jobs)
        [military new-rally-jobs] (maybe-rally-marines [military messages] (game/get-map game) (game/players game) (game/units game) unit-jobs (game/frame game))]
    [military (into messages conj (concat (or new-scouting-jobs [])
                                          (or new-rally-jobs [])))]))