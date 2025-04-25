(ns pepper.proc.find-worker
  (:require [pepper.proc.proc :as proc]))

(defn handle-find-worker [message] {})

(defn transducer [message]
  (case (::proc/message-type message)
    ::find-worker (handle-find-worker message)))

(def process {:id ::find-worker-process
              :messages {::find-worker ::worker-found}
              :constructor transducer})