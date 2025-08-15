(ns pepper.game.player
  (:refer-clojure :exclude [name force type])
  (:require [pepper.game.color :as color]
            [pepper.game.race :as race])
  (:import
   [bwapi Player Force]))

(defn id [player]
  (:id player))

(defn force [player]
  (:force player))

(defn minerals [player]
  (:minerals player))

(defn gas [player]
  (:gas player))

(defn supply-total [player]
  (:supply-total player))

(defn supply-used [player]
  (:supply-used player))

;;;; self

(defn set-self-id [state player]
  (assoc state :self-id (id player)))

(defn get-self-id [state]
  (:self-id state))

(defn get-self [state]
  (get-in state [:players-by-id (get-self-id state)]))

;;;; players

(defn update-player-by-id [players-by-id player]
  (update players-by-id (:id player) merge player))

(defn update-players-by-id [players-by-id players]
  (reduce update-player-by-id players-by-id players))

(defn update-players [state players]
  (update state :players-by-id update-players-by-id players))

;;;; parsing

(defn parse-player!
  "Reads a bwapi player with a bwapi game"
  [game]
  (fn [player]
    (-> {}
        (assoc :id (bwapi.Player/.getID player))
        (assoc :name (bwapi.Player/.getName player))
        (assoc :race (race/object->keyword (bwapi.Player/.getRace player)))
        (assoc :force (Force/.getID (bwapi.Player/.getForce player)))
        (assoc :color (color/object->keyword (bwapi.Player/.getColor player)))
        (assoc :minerals (bwapi.Player/.minerals player))
        (assoc :gas (bwapi.Player/.gas player))
        (assoc :supply-total (Player/.supplyTotal player))
        (assoc :supply-used (Player/.supplyUsed player)))))