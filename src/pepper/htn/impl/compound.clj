(ns pepper.htn.impl.compound)

;;;; shared

(defn assert-preconditions [{:keys [preconditions]}]
  (cond
    (nil? preconditions) "preconditions is nil"
    (not (seq? (seq preconditions))) "preconditions is not a seq"
    (not (every? fn? preconditions)) "not every precondition is a function"))

(defn assert-method [method]
  (cond
    (nil? method) "method is nil"))

;;;; methods

(def method-rules
  [assert-method
   assert-preconditions])

(defn validate-method [method]
  (->> method-rules
       (map #(% method))
       (remove nil?)
       seq))

(defn assert-every-method-valid [compound-task]
  (let [methods (:methods compound-task)]
    (when-not (every? #((apply every-pred method-rules) %) methods)
      (format "Not every method is valid"))))

(defn method [{:keys [name preconditions tasks]
               :or {name ""
                    preconditions []
                    tasks []}
               :as method}]
  {:name (if (some? name) name "")
   :preconditions (if (vector? preconditions)
                    (into [] preconditions)
                    [])
   :tasks (if (vector? tasks)
            (into [] tasks)
            [])})

;;;; tasks

(defn assert-has-methods-key [compound-task]
  (when (not (contains? compound-task :methods))
    (format "The compound task has no :methods key. Compound task: %s" compound-task)))

(defn assert-methods-not-empty [compound-task]
  (when (not (seq? (seq (:methods compound-task))))
    (format "The list of methods for this compound-task is empty. Compound task: %s" compound-task)))

(def task-rules
  [assert-has-methods-key
   assert-methods-not-empty
   assert-every-method-valid])

(defn validate-task [task]
  (->> task-rules
       (map #(% task))
       (remove nil?)
       seq))

(defn task [{:keys [name methods]
             :or {name "" methods []}
             :as task}]
  {:name name
   :methods (if (vector? methods)
              (into [] methods)
              [])})