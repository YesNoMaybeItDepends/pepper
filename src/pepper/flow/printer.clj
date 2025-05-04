(ns pepper.flow.printer
  (:require
   [clojure.core.async.flow :as flow]))

(defn log [input msg]
  (println "[" (java.util.Date.) "]" input "->" msg))

(defn proc
  "Just prints messages"

  ([] {:ins {:in "anything"}})

  ([args] args)

  ([state transition] state)

  ([state input msg]
   #_(when false
       (log input msg))
   [state {::flow/report [msg]}]))