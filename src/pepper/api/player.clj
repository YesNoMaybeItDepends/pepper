(ns pepper.api.player
  "See https://javabwapi.github.io/JBWAPI/bwapi/Player.html")

(defn get-units
  [player]
  (.getUnits player))

(defn get-race
  [player]
  (.getRace player))

(defn supply-total
  [player]
  (.supplyTotal player))

(defn supply-used
  [player]
  (.supplyUsed player))

(defn get-start-location
  [player]
  (.getStartLocation player))

(defn get-name
  [player]
  (.getName player))

(defn minerals
  [player]
  (.minerals player))

(defn gas
  [player]
  (.gas player))

(defn all-unit-count
  ([player]
   (.allUnitCount player))

  ([player unit-type]
   (.allUnitCount player unit-type)))

(defn get-id
  [player]
  (.getID player))

(defn is-ally?
  [player player-2]
  (.isAlly player player-2))

(defn is-enemy?
  [player]
  (.isEnemy player))