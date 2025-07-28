(ns pepper.game.frame
  (:require [pepper.game.unit :as unit]
            [pepper.game.player :as player])
  (:import (bwapi BWClient Game Player)))

(def frame-keywords #{:frame :units :events})

(defn parse-on-start-data [game]
  {:frame (Game/.getFrameCount game)
   :players (map (player/parse-player! game) (Game/.getPlayers game))
   :self-id (Player/.getID (Game/.self game))})

(defn parse-on-frame-data [game]
  {:frame (Game/.getFrameCount game)
   :units (map (unit/parse-unit! game) (Game/.getAllUnits game))})

(defn with-event [frame event]
  (-> frame
      (update :events conj event)))