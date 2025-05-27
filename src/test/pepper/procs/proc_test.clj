(ns test.pepper.procs.proc-test
  (:require [pepper.procs.proc :as proc]
            [pepper.procs.hello-world :as hello-world]))

(defn unwrap-msg [m]
  (case (count m)
    1 (-> m
          vals
          first
          first)))

(defn mock-hello-world-message
  []
  {:msg "hello"
   :msg-id :hello
   :proc-def (select-keys (hello-world/proc) [:ins])
   :proc-id :hello-world})

(defn test-hello-world []
  (-> (mock-hello-world-message)
      proc/process-message
      unwrap-msg
      proc/process-message
      unwrap-msg
      proc/process-message))