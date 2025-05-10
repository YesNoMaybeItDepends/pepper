(ns pepper.api.player
  "See https://javabwapi.github.io/JBWAPI/bwapi/Player.html")

(defn get-units
  [player]
  {:pre [(some? player)]}
  (.getUnits player))

(defn get-race
  [player]
  {:pre [(some? player)]}
  (.getRace player))

(defn supply-total
  [player]
  {:pre [(some? player)]}
  (.supplyTotal player))

(defn supply-used
  [player]
  {:pre [(some? player)]}
  (.supplyUsed player))

(defn get-start-location
  [player]
  {:pre [(some? player)]}
  (.getStartLocation player))

(defn get-name
  [player]
  {:pre [(some? player)]}
  (.getName player))

(defn minerals
  [player]
  {:pre [(some? player)]}
  (.minerals player))

(defn gas
  [player]
  {:pre [(some? player)]}
  (.gas player))

(defn all-unit-count
  ([player]
   {:pre [(some? player)]}
   (.allUnitCount player))

  ([player unit-type]
   {:pre [(some? player)
          (some? unit-type)]}
   (.allUnitCount player unit-type)))

(defn get-id
  [player]
  {:pre [(some? player)]}
  (.getID player))

(defn is-ally?
  [player player-2]
  {:pre [(some? player)]}
  (.isAlly player player-2))

(defn is-enemy?
  [player]
  {:pre [(some? player)]}
  (.isEnemy player))
