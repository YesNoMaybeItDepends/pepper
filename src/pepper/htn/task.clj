(ns pepper.htn.task
  (:require [pepper.htn.impl.compound :as compound]))

(defn primitive [{:keys [name preconditions effects]}]
  {:name name
   :preconditions (into [] preconditions)
   :effects (into [] effects)})

(defn compound [{:keys [name methods] :as task}]
  (compound/task task))

(defn state [state]
  state)

#_(defn validate-compound-task [{:keys [name methods] :as task}]
    (when (nil)))

#_(defn plan [{:keys [state primitive-tasks compound-tasks]}])

#_(defn transition [])

#_(defn plan [{:keys [state]}])