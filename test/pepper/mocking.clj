(ns pepper.mocking
  (:require [pepper.game.unit :as unit]
            [pepper.game.player :as player]
            [pepper.game.unit-type :as unit-type])
  (:import (org.mockito Mockito)
           (bwapi Game Unit Player)))

(defn when-then [x w t]
  (-> (w x)
      (Mockito/when)
      (.thenReturn t)
      (.getMock)))

(defn mock [class opts]
  (reduce (fn [acc [m v]]
            (when-then acc m v))
          (Mockito/mock class)
          opts))

(defn mock-unit
  "map->obj"
  [unit]
  (mock Unit [[#(Unit/.getID %) (int (unit/id unit))]
              [#(Unit/.getType %) (unit-type/keyword->object (unit/type unit))]]))

(defn mock-player
  "map->obj"
  [player]
  (mock Player [[#(Player/.getID %) (int (player/id player))]]))

(defn mock-game [units]
  (mock Game [[#(Game/.getAllUnits %) units]]))