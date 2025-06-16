(ns pepper.htn.planner
  (:require [clojure.spec.alpha :as s]
            [pepper.htn.impl.utils :as u]
            [pepper.htn.impl.compound :as compound]
            [pepper.htn.impl.primitive :as primitive]
            [pepper.htn.impl.planning :as planning]))

(defn state [state] state)

(defn plan [state domain]
  (planning/decompose state domain))

(defn execute [state plan]
  (reduce (fn [acc curr] (curr acc)) state plan))

(defn task [type map-args]
  (case type
    :primitive (primitive/task map-args)
    :compound (compound/task map-args)
    :method (compound/method map-args)))