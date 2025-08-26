(ns pepper.game
  (:require
   [pepper.game.map :as map]
   [pepper.game.player :as player]
   [pepper.game.unit :as unit]
   [pepper.api :as api]
   [pepper.api.game :as api-game])
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

(defn set-latency-remaining-frames [game latency-remaining-frames]
  (assoc game :latency-remaining-frames latency-remaining-frames))

(defn set-latency-remaining-time [game latency-remaining-time]
  (assoc game :latency-remaining-time latency-remaining-time))

(defn set-map [game map]
  (assoc game :map map))

(defn get-map [game]
  (:map game))

(defn set-players [game players]
  (assoc game :players-by-id players))

(defn players-by-id [game]
  (:players-by-id game))

(defn set-units [game units]
  (assoc game :units-by-id units))

(defn units-by-id [game]
  (:units-by-id game))

(defn units [game]
  (vals (:units-by-id game)))

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

(defn parse-on-start [api]
  (let [game (api/get-game api)
        bwem (api/get-bwem api)]
    (-> {}
        (set-frame (Game/.getFrameCount game))
        (set-players (map (player/parse-player! game) (Game/.getPlayers game)))
        (set-map (map/parse-map-on-start! bwem)))))

(defn update-on-start [{:as game :or {}} {:keys [frame players map]}]
  (-> game
      (set-frame frame)
      (update :players-by-id update-players-by-id players)
      (set-map map)))

(defn parse-on-frame [api]
  (let [client (api/get-client api)
        game (api/get-game api)]
    (-> {}
        (set-frame (Game/.getFrameCount game))
        (set-frames-behind (BWClient/.framesBehind client))
        (set-latency-frames (Game/.getLatencyFrames game))
        (set-latency-time (Game/.getLatencyTime game))
        (set-latency-remaining-frames (Game/.getRemainingLatencyFrames game))
        (set-latency-remaining-time (Game/.getRemainingLatencyTime game))
        (set-players (map (player/parse-player! game) (Game/.getPlayers game)))
        (set-units (map (unit/parse-unit! game) (Game/.getAllUnits game))))))

(defn update-on-frame [game {:keys [frame
                                    frames-behind
                                    latency-frames
                                    latency-time
                                    latency-remaining-frames
                                    latency-remaining-time
                                    players-by-id units-by-id]}]
  (-> game
      (set-frame frame)
      (set-frames-behind frames-behind)
      (set-latency-frames latency-frames)
      (set-latency-time latency-time)
      (set-latency-remaining-frames latency-remaining-frames)
      (set-latency-remaining-time latency-remaining-time)
      (update :players-by-id update-players-by-id players-by-id)
      (update :units-by-id update-units-by-id units-by-id)))

(defn render-game! [game api]
  (api-game/draw-text-screen
   (api/get-game api) 100 100
   (str "Frame: " (frame game))))