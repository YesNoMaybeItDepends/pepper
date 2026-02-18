(ns pepper.htn.impl.compound
  (:require
   [clojure.spec.alpha :as s]))

(s/def :task/name keyword?)
(s/def :task/preconditions (s/coll-of ifn? :kind vector?))
(s/def :task/subtask (s/or :primitive :task/primitive :compound :task/compound))
(s/def :task/subtasks (s/coll-of :task/subtask))

(s/def :task/method (s/keys :req [:task/name
                                  :task/preconditions
                                  :task/subtasks]))
(s/fdef method
  :args (s/cat :method :task/method)
  :ret :task/method)

(defn method [method]
  method)

(s/def :task/methods (s/coll-of :task/method))
(s/def :task/compound (s/keys :req [:task/name
                                    :task/methods]))

(s/fdef task
  :args (s/cat :task :task/compound)
  :ret :task/compound)

(defn task [task]
  task)