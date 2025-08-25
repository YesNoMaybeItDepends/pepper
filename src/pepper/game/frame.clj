(ns pepper.game.frame
  (:require [pepper.game.unit :as unit]
            [pepper.game.player :as player]
            [pepper.game.map :as map]
            [pepper.api :as api])
  (:import (bwapi BWClient Game Player)))

(def frame-keywords #{:frame :units :events})

(defn parse-on-start-data
  "For some reason I don't merge the result of this straight into state?
   See state/init-state"
  [api]
  (let [game (api/get-game api)
        bwem (api/get-bwem api)]
    {:frame (Game/.getFrameCount game)
     :players (map (player/parse-player! game) (Game/.getPlayers game))
     :self {:id (Player/.getID (Game/.self game))}
     :map (map/parse-map-on-start! bwem)}))

(defn parse-on-frame-data [api]
  (let [client (api/get-client api)
        game (api/get-game api)]
    {:frame (Game/.getFrameCount game)
     :frames-behind (BWClient/.framesBehind client)
     :latency-frames (Game/.getLatencyFrames game)
     :latency-time (Game/.getLatencyTime game)
     :latency-remaining-frames (Game/.getRemainingLatencyFrames game)
     :latency-remaining-time (Game/.getRemainingLatencyTime game)
     :units (map (unit/parse-unit! game) (Game/.getAllUnits game))
     :players (map (player/parse-player! game) (Game/.getPlayers game))}))

(defn with-event [frame event]
  (-> frame
      (update :events conj event)))

(defn process-frame-data
  "TODO: The idea here is for the on-frame handler to be something like
   1. parse frame data
   2. process frame data
   3. process whatever system (eg: process macro, process micro...)"
  [state frame-data]
  (-> state
      (assoc :frame (:frame frame-data))
      (update :units-by-id unit/update-units-by-id (:units frame-data))
      (update :players-by-id player/update-players-by-id (:players frame-data))))