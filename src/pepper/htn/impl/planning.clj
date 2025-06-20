(ns pepper.htn.impl.planning
  (:require [pepper.htn.impl.utils :as u]))

(declare decompose)

(defn indexed [subtasks]
  (keep-indexed (fn [idx subtask]
                  [idx subtask])
                subtasks))

(defn merge-result [acc {:as curr
                         :keys [state
                                tasks
                                mtr]}]
  (when (some? curr)
    (-> acc
        (assoc :state state)
        (update :tasks into (flatten tasks))
        (update :mtr into (flatten mtr)))))

(defn apply-effect [state effect]
  (effect state))

(defn apply-effects [state effects]
  (reduce apply-effect state effects))

(defn decompose-primitive
  [state {:keys [:task/name
                 :task/preconditions
                 :task/effects
                 :task/operator]
          :as task}
   mtr]
  (if (u/all-true? state preconditions)
    {:state (apply-effects state effects)
     :tasks [{:task/name name
              :mtr mtr
              :task/preconditions preconditions
              :task/operator operator}]
     :mtr mtr}
    nil))

(defn decompose-method
  "Returns map of :state and :tasks"
  [state
   {:as method
    :keys [:task/preconditions
           :task/subtasks]}
   [tidx midx]]
  (when (u/all-true? state preconditions)
    (reduce (fn [acc
                 [sidx subtask]]
              (if-let [res (decompose (:state acc) subtask [tidx midx sidx])]
                (merge-result acc res)
                (reduced nil)))
            {:state state
             :tasks []
             :mtr [midx]}
            (indexed subtasks))))

(defn decompose-compound
  "Returns map of :state and :tasks"
  [state
   {:keys [:task/name :task/methods]
    :as task}
   tidx]
  (reduce (fn [_ [midx method]]
            (when-let [res (decompose-method state method [tidx midx])]
              (reduced (merge-result {:state state
                                      :tasks []
                                      :mtr []}
                                     res))))
          {:state state
           :tasks []
           :mtr [tidx]}
          (indexed methods)))

(defn decompose [state decomposable idx]
  (condp apply [decomposable]
    u/primitive? (decompose-primitive state decomposable idx)
    u/compound? (decompose-compound state decomposable idx)
    nil))