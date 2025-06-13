(ns pepper.htn.impl.primitive)

;; effects are USED DURING PLANNING
;; operator is the ACTUAL EXECUTION

#_(defn normalize [{:keys [name preconditions effects operator]}]
    {:name (if (keyword? name)
             name
             nil)})

#_(defn validate [{:keys [name preconditions effects operator] :as task}]
    (if (every? some? [name preconditions effects operator])
      task
      nil))

(defn task

  [{:keys [name preconditions effects operator]
    :or {name ""
         preconditions []
         effects []
         operator (fn [x] x)}
    :as task}]
  {:name name
   :preconditions (if (vector? preconditions)
                    (into [] preconditions)
                    [])
   :effects (if (vector? effects)
              (into [] effects)
              [])
   :operator (if (fn? operator)
               operator
               (fn [x] x))})

