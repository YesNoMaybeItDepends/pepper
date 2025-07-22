(ns pepper.api.game
  (:import (bwapi Game Text)))

(defn send-text
  [game text]
  (Game/.sendText game text (into-array Text [])))


(defn draw-text-screen
  [game x y text]
  (Game/.drawTextScreen game x y text (into-array Text [])))

(defn set-local-speed
  [game speed]
  (Game/.setLocalSpeed game (speed {:fastest 42
                                    :faster 48
                                    :fast 56
                                    :normal 67
                                    :slow 83
                                    :slower 111
                                    :slowest 167}
                                   42)))