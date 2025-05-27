(ns pepper.procs.proc
  (:require
   [clojure.core.async.flow :as flow]))

(defn init-message
  "proc state + port id + message -> formatted message"
  [{:keys [::flow/pid proc-def]
    :as state} port msg]
  {:proc-id pid
   :proc-def proc-def
   :msg-id port
   :msg msg})

(defn process-message
  "Given messsage, process message. More like format?"
  [{:keys [proc-id proc-def msg-id msg] :as m}]
  (if-let [port-id (msg-id (:ins proc-def))]
    {[proc-id port-id] [(assoc m :msg-id port-id)]}
    {}))