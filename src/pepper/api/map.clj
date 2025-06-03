(ns pepper.api.map)

(defn get-starting-locations
  [map]
  (.getStartingLocations map))

(defn get-unassigned-starting-locations
  [map]
  (.getUnassignedStartingLocations map))