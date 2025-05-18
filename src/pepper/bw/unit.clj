(ns pepper.bw.unit
  (:require
   [pepper.api.player :as player]
   [pepper.api.game :as game]
   [pepper.api.unit :as unit]
   [pepper.api.unit-type :as unit-type]
   [clojure.java.data :as j]))

(defn unit->data
  [unit]
  {:id (unit/get-unit-id unit)
   :player-id (player/get-id (unit/get-player unit))
   :type (unit-type/type->kw (unit/get-type unit))
   :idle? (unit/is-idle? unit)})

(defn units-by-id
  [units]
  (reduce
   (fn [acc curr] (assoc acc (:id curr) curr))
   {} (map unit->data units)))

(defn owned-by-player? [p-id {:keys [player-id] :as unit}]
  (= player-id p-id))

(defn type? [is-type {:keys [type] :as unit}]
  (= type is-type))