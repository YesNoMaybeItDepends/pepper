(ns pepper.htn.task
  (:require
   [pepper.htn.impl.primitive :as primitive]
   [pepper.htn.impl.compound :as compound]))

(defn primitive
  "A primitive task.
   
   Parameters:
   - `task` - a map with the following keys:
     - :name - the name of the compound task
     - :preconditions - list of preconditions
     - :effects - list of effects
     - :operator - operator function
       - should it be `(fn (x) x)` or just side effects?
     
   Returns:
     - task - a map representing a primitive task"
  [task]
  (primitive/task task))

(defn compound
  "A compound task.

   Parameters:
   - `task` - a map of:
     - :name - the name of the compound task
     - :methods - list of methods
   
   Returns:
   - task - a map representing a compound task"
  [task]
  (compound/task task))

(defn state [state]
  state)

#_(defn validate-compound-task [{:keys [name methods] :as task}]
    (when (nil)))

#_(defn plan [{:keys [state primitive-tasks compound-tasks]}])

#_(defn transition [])

#_(defn plan [{:keys [state]}])