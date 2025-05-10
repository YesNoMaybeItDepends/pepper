(ns pepper.api.bwem
  (:require [clojure.java.data :as j]
            [portal.api :as p])
  (:import (bwem BWEM BWMap)))

(defonce bwem nil)
(defn bind-bwem! [b] (alter-var-root #'bwem (constantly b)))
(defn init [game]
  (let [bwem (new BWEM game)]
    (.setFailOnError bwem false)
    (.setFailOutputStream bwem nil)
    (.initialize bwem)
    (.setFailOutputStream bwem System/err)
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

(comment

  bwem

  (j/from-java-shallow bwem {})

  (-> bwem
      (j/from-java-shallow {})
      :map
      (j/from-java-shallow {}))


  #_())