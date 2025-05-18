(ns pepper.api.unit
  "See https://javabwapi.github.io/JBWAPI/bwapi/Unit.html"
  (:require [clojure.java.data :as j]
            [pepper.api.unit-type :as unit-type]
            [pepper.api.game :as game]))

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

(defn get-player
  [unit]
  (.getPlayer unit))

(defn unit->data
  [unit]
  {:id (get-unit-id unit)
   :type (unit-type/type->kw (get-type unit))})

(defn data->unit
  [game {:keys [id] :as data}]
  (game/get-unit game id))

(defn get-from-id
  [game id]
  (game/get-unit game id))

(defn id->unit
  [game id]
  (game/get-unit game id))

(defn get-game-unit-from-id
  [])

(defn get-unit-by-id
  [game {:keys [id] :as unit}]
  (game/get-unit game id))

(defn datafy-unit
  [unit])

(defn units-by-id
  [units]
  (reduce
   (fn [acc curr] (assoc acc (:id curr) curr))
   {} (map unit->data units)))

#_(comment (map unit->data (game/get-all-units)))

#_(comment (j/from-java-shallow (first (game/get-all-units)) {}))

#_(defn by-id-from-game [])