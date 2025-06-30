(ns pepper.core
  (:require
   [clojure.core.async :as a]
   [clojure.core.async.flow :as flow]
   [clojure.core.async.flow-monitor :as flow-monitor]
   [pepper.api.client :as client]
   [pepper.api.game :as g]
   [pepper.procs.handler :as handler]
   [pepper.procs.game :as game]
   [pepper.utils.chaoslauncher :as chaoslauncher]
   [pepper.systems.repl :as repl]
   [taoensso.telemere :as t]
   [pepper.systems.portal :as portal]
   [pepper.systems.flow :as pepper-flow]
   [pepper.systems.logging :as logging]))

(logging/init-logging)

(defn -main
  [store]
  (t/event! :main-start)
  (pepper-flow/flow-main store)
  (t/event! :main-shutting-down)
  (shutdown-agents))