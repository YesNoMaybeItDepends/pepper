(ns pepper.htn.impl.planning
  (:require [pepper.htn.impl.utils :as u]))

(declare decompose)

(defn merge-result [acc {:keys [state tasks] :as curr}]
  (when (some? curr)
    (-> acc
        (assoc :state state)
        (update :tasks into (flatten tasks)))))

(defn decompose-primitive
  [state {:keys [name preconditions operator]
          :as task}]
  (if (u/all-true? state preconditions)
    {:state state
     :tasks [{:name name
              :operator operator}]}
    nil))

(defn decompose-method
  "Returns map of :state and :tasks"
  [state {:keys [preconditions subtasks] :as method}]
  (when (u/all-true? state preconditions)
    (reduce (fn [acc subtask]
              (if-let [res (decompose state subtask)]
                (merge-result acc res)
                (reduced nil)))
            {:state state
             :tasks []}
            subtasks)))

(defn decompose-compound
  "Returns map of :state and :tasks"
  [state
   {:keys [name methods]
    :as task}]
  (reduce (fn [_ method]
            (when-let [res (decompose-method state method)]
              (reduced (merge-result {:state state
                                      :tasks []} res))))
          {}
          methods))

(defn decompose [state decomposable]
  (condp apply [decomposable]
    u/primitive? (decompose-primitive state decomposable)
    u/compound? (decompose-compound state decomposable)
    nil))