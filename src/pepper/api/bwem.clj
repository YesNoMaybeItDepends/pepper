(ns pepper.api.bwem
  (:require [pepper.api.game :as game])
  (:import (bwem BWEM BWMap)))

(defonce bwem nil)
(defn bind-bwem! [b] (alter-var-root #'bwem (constantly b)))
(defn init []
  (let [bwem (new BWEM game/game)]
    (.setFailOnError bwem false)
    (.initialize bwem)
    (.setFailOnError bwem true)
    bwem))

(defn get-map
  []
  (.getMap bwem))

(defn get-starting-locations
  []
  (.getStartingLocations (get-map)))

(defn get-unassigned-starting-locations []
  (.getUnassignedStartingLocations (get-map)))

(defn get-bases [])

