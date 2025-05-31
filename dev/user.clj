(ns user
  (:require
   [pepper.utils.portal :as portal]
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
   [pepper.procs.hello-world :as hello-world]
   [flow-storm.api :as fs-api]
   [taoensso.telemere :as t]))

(defn init? [k]
  (k {:portal true
      :flowstorm false
      :pepper false}))

(def portal (when (init? :portal)
              (atom (portal/start!))))

(def flowstorm (when (init? :flowstorm)
                 (fs-api/local-connect)
                 true))

(comment (try (pepper/-main)
              (catch Exception e (println e)))
         #_())

(comment ;;;; Test flow  

  (do
    (def f (flow/create-flow {:procs {:hello-world {:proc (flow/process #'hello-world/proc)}}}))
    (def chs (flow/start f))
    (def report-chan (:report-chan chs))
    (def error-chan (:error-chan chs))
    (flow/resume f))

  (a/poll! report-chan)
  (a/poll! error-chan)

  (flow/ping f)
  (flow/ping-proc f :handler)

  @(flow/inject f [:hello-world :hello] ["good morning"])

  (flow/stop f)

  #_())