(ns pepper.mocking
  (:require [pepper.game.unit :as unit]
            [pepper.game.unit-type :as unit-type])
  (:import (org.mockito Mockito)
           (bwapi Game Unit)))

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
  "mock a bwapi unit from pure data"
  [unit]
  (mock Unit [[#(Unit/.getID %) (int (unit/id unit))]
              [#(Unit/.getType %) (unit-type/keyword->object (unit/type unit))]]))

(defn mock-game [units]
  (mock Game [[#(Game/.getAllUnits %) units]]))