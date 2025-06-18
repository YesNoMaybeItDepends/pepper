(ns pepper.htn.planner
  (:require [clojure.spec.alpha :as s]
            [pepper.htn.impl.compound :as compound]
            [pepper.htn.impl.primitive :as primitive]
            [pepper.htn.impl.planning :as planning]))

(defn state [state]
  state)

(defn plan
  "Takes a state and a root compound task
   
   Outputs a list of primitive tasks"
  [state domain]
  (:tasks (planning/decompose state domain)))

(defn execute
  "Taking a list of primitive tasks, applies their operators to the given state, returning the final state"
  [state plan]
  (reduce (fn [state task] ((:task/operator task) state)) state plan))

(s/fdef task
  :args (s/cat :type #{:primitive :compound :method}
               :map (s/or :primitive-map :task/primitive
                          :compound-map :task/compound
                          :method-map :task/method)))

(defn task [type map-args]
  (case type
    :primitive (primitive/task map-args)
    :compound (compound/task map-args)
    :method (compound/method map-args)))