(ns pepper.flow.printer)

(defn log [input msg]
  (println "[" (java.util.Date.) "]" input "->" msg))

(defn proc
  "Just prints messages"

  ([] {:ins {:in "anything"}})

  ([args] args)

  ([state transition] state)

  ([state input msg]
   (when false
     (log input msg))
   [state]))