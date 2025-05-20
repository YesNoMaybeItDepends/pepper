(ns pepper.procs.printer
  (:require [clojure.core.async :as a]
            [clojure.core.async.flow :as flow]
            [clojure.core.async.flow-monitor :as mon]))

(defn printer
  ;; describe
  ([] {:params {:prefix "Log message prefix"}
       :ins {:in "Channel to receive messages"}})

  ;; init
  ([state] state)

  ;; transition
  ([state _transition] state)

  ;; transform
  ([{:keys [prefix] :as state} _in msg]
   (println prefix msg)
   [state nil]))