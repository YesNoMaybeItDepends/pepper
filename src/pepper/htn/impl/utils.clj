(ns pepper.htn.impl.utils)

(defn all-true? [state predicates]
  (reduce (fn [_ pred]
            (if (pred state)
              true
              (reduced nil)))
          true
          predicates))

(defn primitive? [m]
  (some? (:task/operator m)))

(defn compound? [m]
  (some? (:task/methods m)))