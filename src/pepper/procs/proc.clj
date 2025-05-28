(ns pepper.procs.proc
  (:require
   [clojure.core.async.flow :as flow]))

(defn state->pid
  [state]
  (::flow/pid state))

(defn state->proc-def
  [state]
  (:proc-def state))

(defn msg->port-id
  [{:keys [msg-id proc-def] :as msg}]
  (msg-id (:ins proc-def)))

(defn msg->proc-id
  [msg]
  (:proc-id msg))

(defn msg->coord
  [msg]
  [(msg->proc-id msg) (msg->port-id msg)])

(defn init-state
  [args proc-fn]
  (assoc args :proc-def (proc-fn)))

(defn init-message
  "proc state + port id + message -> formatted message"
  [state inid msg]
  {:proc-id (state->pid state)
   :proc-def (state->proc-def state)
   :msg-id inid
   :msg msg})

(defn process-message
  "Given messsage, process message. More like format? Returns {[coord] [msg]}"
  [{:keys [proc-id proc-def msg-id] :as msg}]
  (if-let [port-id (msg->port-id msg)]
    {(msg->coord msg) [(assoc msg :msg-id port-id)]}
    {}))