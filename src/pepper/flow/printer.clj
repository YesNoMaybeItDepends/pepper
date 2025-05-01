(ns pepper.flow.printer)

(defn proc
  "Just prints messages"

  ([] {:ins {:in "anything"}})
  ([args] args)
  ([state transition] state)
  ([state input msg]
   (println "[" (java.util.Date.) "]" input "->" msg)
   [state]))