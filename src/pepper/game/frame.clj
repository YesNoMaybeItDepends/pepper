(ns pepper.game.frame
  (:require [pepper.game.unit :as unit])
  (:import (bwapi BWClient Game)))

(def frame-keywords #{:frame :units :events})

(defn parse-frame-data [game]
  {:frame (Game/.getFrameCount game)
   :units (map (unit/parse-unit game) (Game/.getAllUnits game))})

(defn with-event [frame event]
  (-> frame
      (update :events conj event)))