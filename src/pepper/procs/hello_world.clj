(ns pepper.procs.hello-world
  (:require
   [clojure.core.async.flow :as flow]
   [pepper.procs.proc :as proc]))

(defn proc
  "See the `hello-world-message` for an example message."
  ([] {:ins {:hello :who
             :who :world
             :world nil}})
  ([args] (assoc args :proc-def (proc)))
  ([s t] s)
  ([state port msg]
   (println {:msg msg
             :port port})
   (let [pid (::flow/pid state)]
     (case port
       :hello [state (proc/process-message (proc/init-message state port msg))]
       :who [state (proc/process-message msg)]
       :world (println msg) [state (process-message msg)]
       [state nil]))))