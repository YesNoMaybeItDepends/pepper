(ns pepper.htn.impl.compound)

(defn has-methods-key [compound-task]
  (when (not (contains? compound-task :methods))
    (format "The compound task has no :methods key. Compound task: %s" compound-task)))

(defn methods-not-empty [compound-task]
  (when (not (seq? (seq (:methods compound-task))))
    (format "The list of methods for this compound-task is empty. Compound task: %s" compound-task)))

(def rules
  [has-methods-key
   methods-not-empty])

(defn validate [task]
  (->> rules
       (map #(% task))
       (remove nil?)
       seq))

(defn make [{:keys [name methods]}]
  {:name name
   :methods (into [] methods)})