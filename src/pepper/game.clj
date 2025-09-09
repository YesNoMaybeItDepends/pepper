(ns pepper.game
  (:require
   [clojure.string :as str]
   [pepper.api :as api]
   [pepper.api.game :as api-game]
   [pepper.game.ability :as ability]
   [pepper.game.upgrade :as upgrade]
   [pepper.game.map :as map]
   [pepper.game.player :as player]
   [pepper.game.position :as position]
   [pepper.game.unit :as unit])
  (:import
   [bwapi BWClient Game]))

(defn set-frame [game frame]
  (assoc game :frame frame))

(defn frame [game]
  (:frame game))

(defn set-frames-behind [game frames-behind]
  (assoc game :frames-behind frames-behind))

(defn set-latency-frames [game latency-frames]
  (assoc game :latency-frames latency-frames))

(defn set-latency-time [game latency-time]
  (assoc game :latency-time latency-time))

(defn set-elapsed-time [game elapsed-time]
  (assoc game :elapsed-time elapsed-time))

(defn elapsed-time [game]
  (:elapsed-time game))

(defn set-latency-remaining-frames [game latency-remaining-frames]
  (assoc game :latency-remaining-frames latency-remaining-frames))

(defn set-latency-remaining-time [game latency-remaining-time]
  (assoc game :latency-remaining-time latency-remaining-time))

(defn set-map [game map]
  (assoc game :map map))

(defn get-map [game]
  (:map game))

(defn set-players-by-id [game players-by-id]
  (assoc game :players-by-id players-by-id))

(defn players-by-id [game]
  (:players-by-id game))

(defn players [game]
  (vals (players-by-id game)))

(defn set-units [game units]
  (assoc game :units-by-id units))

(defn units-by-id [game]
  (:units-by-id game))

(defn units [game]
  (vals (units-by-id game)))

(defn player-owns-unit? [player unit]
  (= (player/id player) (unit/player-id unit)))

(defn player-units [player units]
  (filterv #(player-owns-unit? player %) units))

;; our

(defn our-player [game]
  (player/our-player (players game)))

(defn our-units [game]
  (player-units (our-player game) (units game)))

;; enemy

(defn enemy-player [game]
  (player/enemy-player (players game)))

(defn enemy-units [game]
  (player-units (enemy-player game) (units game)))

;; neutral

(defn neutral-player [game]
  (player/neutral-player (players game)))

(defn neutral-units [game]
  (player-units (neutral-player game) (units game)))

;;;;;;;;;

(defn update-units-by-id [units-by-id units]
  (reduce (fn [units-by-id unit]
            (update units-by-id (unit/id unit) unit/update-unit unit))
          (or units-by-id {})
          units))

(defn update-players-by-id [players-by-id players]
  (reduce (fn [players-by-id player]
            (update players-by-id (player/id player) player/update-player player))
          (or players-by-id {})
          players))

;;;;;;;

(defn parse-on-start [api]
  (let [game (api/game api)
        bwem (api/bwem api)]
    (-> {}
        (set-frame (Game/.getFrameCount game))
        (set-elapsed-time (or (Game/.elapsedTime game) 0))
        (set-players-by-id (mapv (player/parse-player! game) (Game/.getPlayers game))) ;; these players are not by id lol, fix this, look at update-on-start
        (set-map (map/parse-map-on-start! bwem)))))

(defn update-on-start [{:as game :or {}} {:keys [frame players-by-id map elapsed-time]}]
  (-> game
      (set-frame frame)
      (set-elapsed-time elapsed-time)
      (update :players-by-id update-players-by-id players-by-id)
      (set-map map)))

(defn parse-on-frame [api]
  (let [client (api/client api)
        game (api/game api)]
    (-> {}
        (set-frame (Game/.getFrameCount game))
        (set-frames-behind (BWClient/.framesBehind client))
        (set-latency-frames (Game/.getLatencyFrames game))
        (set-latency-time (Game/.getLatencyTime game))
        (set-latency-remaining-frames (Game/.getRemainingLatencyFrames game))
        (set-latency-remaining-time (Game/.getRemainingLatencyTime game))
        (set-elapsed-time (Game/.elapsedTime game))
        (set-players-by-id (map (player/parse-player! game) (Game/.getPlayers game)))
        (set-units (map (unit/parse-unit! game) (Game/.getAllUnits game)))
        (merge
         {:can-upgrade (transduce
                        (comp
                         (filter (comp #(Game/.canUpgrade game %) second))
                         (map first))
                        conj
                        upgrade/by-keyword)}
         {:can-research (transduce
                         (comp
                          (filter (comp #(Game/.canResearch game %) second))
                          (map first))
                         conj
                         ability/by-keyword)}))))

(defn update-on-frame
  "TODO: just merge this"
  [game {:keys [frame
                frames-behind
                latency-frames
                latency-time
                latency-remaining-frames
                latency-remaining-time
                elapsed-time
                players-by-id
                units-by-id
                can-upgrade
                can-research]}]
  (-> game
      (set-frame frame)
      (set-frames-behind frames-behind)
      (set-latency-frames latency-frames)
      (set-latency-time latency-time)
      (set-latency-remaining-frames latency-remaining-frames)
      (set-latency-remaining-time latency-remaining-time)
      (set-elapsed-time elapsed-time)
      (update :players-by-id update-players-by-id players-by-id)
      (update :units-by-id update-units-by-id units-by-id)
      (assoc :can-upgrade can-upgrade)
      (assoc :can-research can-research)))

(defn unit-position [unit-obj]
  (position/->map (.getPosition unit-obj)))

(defn render-units! [units game]
  (doseq [unit-id (filterv some? (mapv unit/id units))]
    (let [u (.getUnit game unit-id)
          {:keys [x y]} (unit-position u)]
      (when (and x y)
        (if (.isCompleted u)
          (api-game/draw-text-map game x y (str unit-id))
          (api-game/draw-text-map game x (+ y 10) (str "-> " unit-id)))))))

(defn clock [seconds]
  (let [seconds (or seconds 0)
        minutes (quot seconds 60)
        seconds (mod seconds 60)
        format (fn [n] (format "%02d" n))]
    (->> [minutes seconds]
         (map format)
         (str/join ":"))))

(defn render-top-left! [api strings]
  (api-game/draw-text-screen
   (api/game api) 2 0 (str/join "\n" strings)))

(defn render-game! [game api]
  (let [format-fn (fn [[k v]] [(str k " " v)])
        display {:frame (frame game)
                 :clock (clock (elapsed-time game))}]
    (render-top-left!
     api (mapcat format-fn display))
    (render-units! (units game) (api/game api))))

(defn update-on-unit-event [game [event-id {unit :unit}] api]
  (let [frame (Game/.getFrameCount (api/game api))
        with-some-data (fn [u]
                         (merge
                          u (if (:visible? u)
                              (unit/->map unit frame [:id :type :player-id :position :tile])
                              (unit/->map unit frame [:id :initial-type :initial-position :initial-resources]))))
        u (-> (merge {} (case event-id
                          :on-unit-complete {:fame-completed frame}
                          :on-unit-create {:frame-created frame}
                          :on-unit-destroy {:frame-destroyed frame
                                            :visible? false
                                            :exists? false}
                          :on-unit-discover {:frame-discovered frame ;; discover / show / hide ?
                                             :visible? true}
                          :on-unit-evade {:frame-evaded frame}
                          :on-unit-hide {:frame-hidden frame
                                         :visible? false} ;; discover / show / hide ?
                          :on-unit-morph {:frame-morphed frame}
                          :on-unit-renegade {:frame-renegade frame}
                          :on-unit-show {:frame-shown frame ;; discover / show / hide ?
                                         :visible? true}))
              with-some-data)]
    (update game :units-by-id update-units-by-id [u])))
