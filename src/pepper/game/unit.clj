(ns pepper.game.unit
  (:import (bwapi Unit)))

(defn id [unit]
  (if (map? unit)
    (:unit/id unit)
    (Unit/.getID unit)))

(defn datafy [api-unit]
  {:unit/id (id api-unit)})