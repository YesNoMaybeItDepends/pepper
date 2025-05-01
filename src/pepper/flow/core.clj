(ns pepper.flow.core
  (:require
   [clojure.core.async :as a]
   [clojure.core.async.flow :as flow]
   [pepper.flow.client :as flow-client]
   [pepper.flow.printer :as flow-printer]))

(defn create-flow
  []
  (flow/create-flow
   {:procs {:client {:proc
                     (flow/process #'flow-client/proc)}

            :printer {:proc
                      (flow/process #'flow-printer/proc)}}

    :conns [[[:client :out] [:printer :in]]]}))

(defonce f (create-flow))
#_(defonce chs (flow/start f))

(comment

  (def chs (flow/start f))
  (flow/resume f)
  (flow/pause f)
  (flow/stop f)

  (def report-chan (:report-chan chs))
  (a/poll! report-chan)
  (def error-chan (:error-chan chs))
  (a/poll! error-chan)

  #_())