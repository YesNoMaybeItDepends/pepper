(ns pepper.game.player
  (:refer-clojure :exclude [name force type])
  (:require [clojure.set :as set])
  (:import
   (bwapi BWClient Game Player Unit)))

(defn get-self [state]
  (get-in state [:players-by-id (:self-player-id state)]))

(defn update-player-by-id [players-by-id player]
  (update players-by-id (:id player) merge player))

(defn update-players-by-id [players-by-id players]
  (reduce update-player-by-id players-by-id players))

(defn parse-player!
  "Reads a bwapi player with a bwapi game"
  [game]
  (fn [player]
    (-> {}
        (assoc :id (bwapi.Player/.getID player))
        (assoc :name (bwapi.Player/.getName player))
        (assoc :force (bwapi.Player/.getForce player))
        (assoc :color (bwapi.Player/.getColor player))
        (assoc :minerals (bwapi.Player/.minerals player))
        (assoc :gas (bwapi.Player/.gas player))
        (assoc :race (bwapi.Player/.getRace player)))))