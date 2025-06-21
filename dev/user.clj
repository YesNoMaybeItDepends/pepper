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
   [zprint.core :as zp]
   [clojure.spec.alpha :as s]
   [pepper.core :as pepper]
   [pepper.procs.hello-world :as hello-world]
   [flow-storm.api :as fs-api]
   [taoensso.telemere :as t]
   [pepper.utils.chaoslauncher :refer [stop!]]
   #_[clojure.spec.test.alpha :as st]
   [portal.api :as p]
   [clojure.tools.namespace.find :as ns-find]
   [clojure.java.classpath :as cp]
   [clojure.tools.namespace.parse :as ns-parse]
   [clojure.string :as str]
   [user.specs :as specs]))

(defn init? [k]
  (k {:portal true
      :flowstorm false
      :pepper false
      :instrument true
      :zprint true
      :tests true}))

(def portal (when (init? :portal)
              (atom (portal/start!))))

(def flowstorm (when (init? :flowstorm)
                 (fs-api/local-connect)
                 true))

(when (init? :zprint)
  (add-tap zp/zprint))

(when (init? :instrument)
  (specs/instrument))

(comment (try (pepper/-main)
              (catch Exception e (println e)))
         #_())

(comment "Trying to load all test namespaces"

         (defn is-test-dir [file]
           (and (true? (.isDirectory file))
                (= "test" (.getName file))))

         (defn find-test-dir []
           (first (filter is-test-dir (cp/classpath))))

         (defn find-test-namespaces []
           (ns-find/find-namespaces-in-dir (find-test-dir)))

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

