(ns pepper.game
  (:require
   [pepper.game.map :as map]
   [pepper.game.player :as player]
   [pepper.game.unit :as unit])
  (:import
   [bwapi BWClient Game]))

(defn set-frame [gaem frame]
  (assoc gaem :frame frame))

(defn set-frames-behind [gaem frames-behind]
  (assoc gaem :frames-behind frames-behind))

(defn set-latency-frames [gaem latency-frames]
  (assoc gaem :latency-frames latency-frames))

(defn set-latency-time [gaem latency-time]
  (assoc gaem :latency-time latency-time))

(defn set-latency-remaining-frames [gaem latency-remaining-frames]
  (assoc gaem :latency-remaining-frames latency-remaining-frames))

(defn set-latency-remaining-time [gaem latency-remaining-time]
  (assoc gaem :latency-remaining-time latency-remaining-time))

(defn set-map [gaem map]
  (assoc gaem :map map))

(defn set-players [gaem players]
  (assoc gaem :players players))

(defn set-units [gaem units]
  (assoc gaem :units units))

(defn parse-on-start [api]
  (let [game (api :game)
        bwem (api :bwem)]
    (-> {}
        (set-frame (Game/.getFrameCount game))
        (set-players (map (player/parse-player! game) (Game/.getPlayers game)))
        ;; :self {:id (Player/.getID (Game/.self game))}
        (set-map (map/parse-map-on-start! bwem)))))

(defn update-on-start [gaem {:keys [frame players map]}]
  (-> gaem
      (set-frame frame)
      (set-players players)
      (set-map map)))

(defn parse-on-frame [api]
  (let [client (api :client)
        game (api :game)]
    (-> {}
        (set-frame (Game/.getFrameCount game))
        (set-frames-behind (BWClient/.framesBehind client))
        (set-latency-frames (Game/.getLatencyFrames game))
        (set-latency-time (Game/.getLatencyTime game))
        (set-latency-remaining-frames (Game/.getRemainingLatencyFrames game))
        (set-latency-remaining-time (Game/.getRemainingLatencyTime game))
        (set-players (map (player/parse-player! game) (Game/.getPlayers game)))
        (set-units (map (unit/parse-unit! game) (Game/.getAllUnits game))))))

(defn update-on-frame [gaem {:keys [frame
                                    frames-behind
                                    latency-frames
                                    latency-time
                                    latency-remaining-frames
                                    latency-remaining-time
                                    players units]}]
  (-> gaem
      (set-frame frame)
      (set-frames-behind frames-behind)
      (set-latency-frames latency-frames)
      (set-latency-time latency-time)
      (set-latency-remaining-frames latency-remaining-frames)
      (set-latency-remaining-time latency-remaining-time)
      (set-players players)
      (set-units units)))