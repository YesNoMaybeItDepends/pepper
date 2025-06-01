(ns pepper.api.game
  (:import (bwapi Game Text)))

(defn with-game
  [f & args]
  (fn [game]
    (apply f game args)))

;;;;

(defn get-frame-count [game]
  (.getFrameCount game))

(defn draw-text-screen [game x y text]
  (if game (.drawTextScreen game x y text (into-array Text []))
      (println "got no game sire...")))

(defn pause-game
  [game]
  (.pauseGame game))

(defn set-local-speed
  [game speed]
  (.setLocalSpeed game (speed {:fastest 42
                               :faster 48
                               :fast 56
                               :normal 67
                               :slow 83
                               :slower 111
                               :slowest 167}
                              42)))