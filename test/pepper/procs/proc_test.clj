#_(ns test.pepper.procs.proc-test
    #_(:require [pepper.procs.proc :as proc]
                [clojure.spec.test.alpha :as st]
                [pepper.procs.hello-world :as hello-world]))

#_(st/instrument)

#_(defn unwrap-msg [m]
    (case (count m)
      1 (-> m
            vals
            first
            first)))

#_(defn mock-hello-world-message
    []
    {:msg "hello"
     :msg-id :hello
     :proc-def (select-keys (hello-world/proc) [:ins])
     :proc-id :hello-world})

#_(defn test-hello-world []
    (-> (mock-hello-world-message)
        proc/process-message
        unwrap-msg
        proc/process-message
        unwrap-msg
        proc/process-message))