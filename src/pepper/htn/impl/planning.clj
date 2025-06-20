(ns pepper.htn.impl.planning
  (:require [pepper.htn.impl.utils :as u]))

(declare decompose)

(defn merge-result [acc {:keys [state tasks] :as res}]
  (when (some? res)
    (-> acc
        (assoc :state state)
        (update :tasks into (flatten tasks)))))

(defn apply-effect [state effect]
  (effect state))

(defn apply-effects [state effects]
  (reduce apply-effect state effects))

(defn decompose-primitive
  [state {:keys [:task/name
                 :task/preconditions
                 :task/effects
                 :task/operator]
          :as task}]
  (if (u/all-true? state preconditions)
    {:state (apply-effects state effects)
     :tasks [{:task/name name
              :task/preconditions preconditions
              :task/operator operator}]}
    nil))

(defn decompose-method
  "Returns map of :state and :tasks"
  [state {:keys [:task/preconditions :task/subtasks] :as method}]
  (when (u/all-true? state preconditions)
    (reduce (fn [acc subtask]
              (if-let [res (decompose (:state acc) subtask)]
                (merge-result acc res)
                (reduced nil)))
            {:state state
             :tasks []}
            subtasks)))

(defn decompose-compound
  "Returns map of :state and :tasks"
  [state
   {:keys [:task/name :task/methods]
    :as task}]
  (reduce (fn [_ method]
            (when-let [res (decompose-method state method)]
              (reduced (merge-result {:state state
                                      :tasks []}
                                     res))))
          {}
          methods))

(defn decompose [state decomposable]
  (condp apply [decomposable]
    u/primitive? (decompose-primitive state decomposable)
    u/compound? (decompose-compound state decomposable)
    nil))