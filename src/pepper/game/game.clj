(ns pepper.game.game
  (:require [pepper.game.unit :as unit])
  (:import (bwapi Game)))

(defn frame [state]
  (or (:game/frame state)
      -1))

(defn units-by-id [state]
  (or (:game/units-by-id state)
      {}))

(defn units-by-id-ids [state]
  (if-some [unit-ids (keys (units-by-id state))]
    unit-ids
    '()))

(defn update-unit [state unit]
  (update-in state [:game/units-by-id (unit/id unit)] merge unit))

(defn update-units [state units]
  (reduce update-unit state units))

(defn new-unit-id? [state id]
  (nil? (get (units-by-id state) id)))

(defn filter-new-units [state ids]
  (filter #(new-unit-id? state %) ids))

(defn map-new-units [{:api/keys [game] :as state} ids]
  (map #((unit/read-game-unit game) %) ids))