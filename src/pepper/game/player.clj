(ns pepper.game.player
  (:refer-clojure :exclude [name force type])
  (:require [pepper.game.color :as color]
            [pepper.game.race :as race]
            [pepper.game.position :as position])
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

(defn supply-available [player]
  (let [total (supply-total player)
        used (supply-used player)]
    (- total used)))

(defn starting-base [player]
  (:starting-base player))

;;;; players

(defn update-player [player new-player]
  (merge player new-player))

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
        (assoc :supply-used (Player/.supplyUsed player))
        (assoc :starting-base (position/->data (Player/.getStartLocation player))))))