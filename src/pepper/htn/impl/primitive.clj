(ns pepper.htn.impl.primitive
  (:require [clojure.spec.alpha :as s]))

(s/def :task/name           keyword?)
(s/def :task/preconditions  (s/coll-of fn? :kind vector?))
(s/def :task/effects        (s/coll-of fn? :kind vector?))
(s/def :task/operator       fn?)
(s/def :task/primitive      (s/keys :req [:task/name
                                          :task/preconditions
                                          :task/effects
                                          :task/operator]))

(s/def :task/args (s/cat :task :task/primitive))
(s/def :task/ret  :task/primitive)
(s/def :task/fn   (fn [{:keys [args ret]}] #_()))
(s/fdef task
  :args :task/args
  :ret :task/ret)

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

