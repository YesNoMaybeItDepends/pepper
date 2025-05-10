(ns pepper.api.unit
  "See https://javabwapi.github.io/JBWAPI/bwapi/Unit.html")

(defn get-type
  [unit]
  {:pre [(some? unit)]}
  (.getType unit))

(defn get-unit-id
  [unit]
  {:pre [(some? unit)]}
  (.getID unit))

(defn build
  [unit building tile-position]
  {:pre [(some? unit)
         (some? building)
         (some? tile-position)]}
  (.build unit building tile-position))

(defn is-idle?
  [unit]
  {:pre [(some? unit)]}
  (.isIdle unit))

(defn is-gathering-minerals?
  [unit]
  {:pre [(some? unit)]}
  (.isGatheringMinerals unit))

(defn is-completed?
  [unit]
  {:pre [(some? unit)]}
  (.isCompleted unit))

(defn exists?
  [unit]
  {:pre [(some? unit)]}
  (.exists unit))

(defn train
  [unit unit-type]
  {:pre [(some? unit)
         (some? unit-type)]}
  (.train unit unit-type))

(defn gather
  "Gather target unit with unit"
  [unit target]
  {:pre [(some? unit)
         (some? target)]}
  (.gather unit target))

(defn get-distance
  "TODO: target is either position or unit"
  [unit target]
  {:pre [(some? unit)
         (some? target)]}
  (.getDistance unit target))

(defn get-training-queue-count
  [unit]
  {:pre [(some? unit)]}
  (.getTrainingQueueCount unit))

(defn get-build-unit
  [unit]
  {:pre [(some? unit)]}
  (.getBuildUnit unit))