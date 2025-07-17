(ns pepper.game.game
  (:require [pepper.game.unit :as unit])
  (:import (bwapi Game)))

(defn find-new-units [{:game/keys [units-by-id]} _game]
  (->> (Game/.getAllUnits _game)
       (mapv unit/id)
       (remove units-by-id)
       (into #{})))

(defn get-units-by-id [state]
  (or (:game/units-by-id state)
      {}))

(defn get-new-units [state]
  (or (:game/new-units-by-id state)
      #{}))

(defn update-state [state _game]
  {:game/units-by-id (get-units-by-id state)
   :game/new-units-by-id (conj (get-new-units state)
                               (find-new-units state _game))})

(defn get-state [state]
  {:game/units-by-id (get-units-by-id state)
   :game/new-units-by-id (get-new-units state)})