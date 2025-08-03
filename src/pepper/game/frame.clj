(ns pepper.game.frame
  (:require [pepper.game.unit :as unit]
            [pepper.game.player :as player])
  (:import (bwapi BWClient Game Player)))

(def frame-keywords #{:frame :units :events})

(defn parse-on-start-data [game]
  {:frame (Game/.getFrameCount game)
   :players (map (player/parse-player! game) (Game/.getPlayers game))
   :self {:id (Player/.getID (Game/.self game))}})

(defn parse-on-frame-data [game]
  {:frame (Game/.getFrameCount game)
   :units (map (unit/parse-unit! game) (Game/.getAllUnits game))
   :players (map (player/parse-player! game) (Game/.getPlayers game))})

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
      (update :player-by-id player/update-players-by-id (:players frame-data))))