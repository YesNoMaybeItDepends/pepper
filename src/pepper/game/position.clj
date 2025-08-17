(ns pepper.game.position
  (:import [bwapi Point Position TilePosition WalkPosition]))

(defn ->bwapi
  [[x y :as position] to]
  (case to
    :position (Position. x y)
    :tile-position (TilePosition. x y)
    :walk-position (WalkPosition. x y)))

(defn ->data [position]
  [(Point/.getX position)
   (Point/.getY position)])