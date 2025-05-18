(ns pepper.api.bwem
  (:require [clojure.java.data :as j])
  (:import (bwem BWEM BWMap)))

#_(defonce ^:dynamic *bwem* nil)

#_(defn bind-bwem! [b] (alter-var-root #'*bwem* (constantly b)))

(defn init [game]
  (let [bwem (new BWEM game)]
    (.setFailOnError bwem false)
    (.setFailOutputStream bwem nil)
    (.initialize bwem)
    (.setFailOutputStream bwem System/err)
    (.setFailOnError bwem true)
    bwem))

(defn get-map
  [bwem]
  (.getMap bwem))

(defn get-starting-locations
  [bwem]
  (.getStartingLocations (get-map bwem)))

(defn get-unassigned-starting-locations [bwem]
  (.getUnassignedStartingLocations (get-map bwem)))