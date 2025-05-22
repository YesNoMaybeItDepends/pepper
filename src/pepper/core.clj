(ns pepper.core
  (:require
   [clojure.spec.alpha :as s]
   [clojure.core.async :as a]
   [clojure.core.async.flow :as flow]
   [clojure.core.async.flow-monitor :as flow-mon]
   [pepper.procs.client :as client]
   [pepper.procs.printer :as printer]))

(defn main-flow []
  (flow/create-flow {:procs {:client {:proc (flow/process #'client/proc)
                                      :args {:client-config {:async false
                                                             :debug-connection true
                                                             :log-verbosely true}
                                             :ch-from-client (a/chan 10)
                                             :ch-to-client (a/chan 10)}}
                             :printer {:proc (flow/process #'printer/printer)}}

                     :conns [[[:client :out] [:printer :in]]]}))

(defn -main []
  (let [f (main-flow)
        chs (flow/start f)]
    (flow/resume f)
    {:flow f :chs chs}))