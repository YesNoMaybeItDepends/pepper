(ns pepper.systems.flow-monitor
  (:require
   [clojure.core.async.flow-monitor :as flow-monitor]))

(defonce flow-monitor false)

(defn init-flow-monitor
  "TODO: flow-monitor throws error"
  [_flow]
  (when (true? flow-monitor)
    (alter-var-root #'flow-monitor (fn [x] (flow-monitor/start-server _flow)))))