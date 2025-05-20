(ns user
  (:require
   [user.portal :refer [start-portal!]]
   [portal.api :as p]
   [clojure.reflect :as reflect]
   [clojure.core.async :as a]
   [clojure.core.async.flow :as flow]
   [clojure.core.async.flow-monitor :as flow-monitor]
   [clojure.java.process :as process]
   [clojure.java.io :as io]
   [clojure.repl :as repl]
   [clojure.pprint :as pprint]
   [zprint.zprint :as zp]
   [clojure.spec.alpha :as s]
   [pepper.core :as pepper]
   [flow-storm.api :as fs-api]))

(defonce portal (atom (start-portal!)))
(defonce flowstorm (do (fs-api/local-connect)
                       true))

(when true
  (def bot (pepper/-main))
  (def report-chan (:report-chan (:chs bot)))
  (def error-chan (:error-chan (:chs bot)))
  (def f (:flow bot))
  (def server (flow-monitor/start-server {:flow f})))

(comment

  (a/poll! error-chan)
  (a/poll! report-chan)

  (flow/ping f)
  (flow/ping-proc f :client)
  (flow/ping-proc f :printer)

  #_())


(comment "testing api inputs"

         (def out-ch (a/chan (a/sliding-buffer 1)))
         (def out-fn (partial a/put! out-ch))
         (def client-fn (fn callback [e] (f e)))
         (defn event-handler [f] (f 1))

         #_())