(ns pepper.htn.planner
  (:require [clojure.spec.alpha :as s]))

(defn empty-sequence? [coll]
  (not (seq? (seq coll))))

(defn all-preconditions-true [preconditions x]
  (if (empty-sequence? preconditions)
    true
    (if ((apply every-pred (into [] preconditions)) x)
      true
      false)))

(defn plan [{:keys [methods] :as compound-task} state]
  (-> (reduce (fn [acc {:keys [preconditions effects] :as method}]
                (if (all-preconditions-true preconditions state)
                  (conj acc effects)
                  acc))
              [] methods)
      flatten))

(defn execute [plan state]
  (reduce (fn [acc curr] (curr acc)) state plan))