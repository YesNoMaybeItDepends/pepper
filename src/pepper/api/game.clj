(ns pepper.api.game
  (:import (bwapi Game Text)))

(defn get-frame-count [game]
  (.getFrameCount game))

(defn draw-text-screen [game x y text]
  (if game (.drawTextScreen game x y text (into-array Text []))
      (println "got no game sire...")))
