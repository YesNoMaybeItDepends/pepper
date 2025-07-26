(ns pepper.game.player
  (:refer-clojure :exclude [name force type])
  (:require [clojure.set :as set])
  (:import
   (bwapi BWClient Game Player Unit)))

(defn id [player]
  (:player/id player))

(defn with-id [player id]
  (assoc player :player/id id))

(defn units [player]
  (or (:player/units player)
      #{}))

(defn with-units
  "TODO: does assoc, consider a more flexible update"
  [player units]
  (if (set? units)
    (assoc player :player/units units)
    (assoc player :player/units (set units))))

(defn with-unit [player unit]
  (update player :player/units set/union #{unit}))

(defn name [player]
  (:player/name player))

(defn with-name [player name]
  (assoc player :player/name name))

(defn color [player]
  (:player/color player))

(defn with-color [player color]
  (assoc player :player/color color))

(defn force [player]
  (:player/force player))

(defn with-force [player force]
  (assoc player :player/force force))

(defn race [player]
  (:player/race player))

(defn with-race [player race]
  (assoc player :player/race race))

(defn type [player]
  (:player/type player))

(defn with-type [player type]
  (assoc player :player/type type))

(defn neutral? [player]
  (:player/neutral? player))

(defn with-neutral? [player neutral?]
  (assoc player :player/neutral? neutral?))

(defn observer? [player]
  (:player/observer? player))

(defn with-observer? [player observer?]
  (assoc player :player/observer? player))

(defn read-game-player [game]
  (fn [player]
    (-> {}
        (with-id (bwapi.Player/.getID player))
        (with-name (bwapi.Player/.getName player))
        (with-color (bwapi.Player/.getColor player))
        (with-force (bwapi.Player/.getForce player))
        (with-race (bwapi.Player/.getRace player))
        (with-type (bwapi.Player/.getType player))
        (with-neutral? (bwapi.Player/.isNeutral player))
        (with-observer? (bwapi.Player/.isObserver player))
        (with-units (->> (bwapi.Player/.getUnits player)
                         (mapv bwapi.Unit/.getID))))))