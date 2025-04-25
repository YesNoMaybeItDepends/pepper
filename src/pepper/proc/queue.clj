(ns pepper.proc.queue
  "This should be the main message bus, all requests get thrown in here for procs to take, procs may also put messages back"
  (:require [clojure.core.async :refer [chan pub]]
            [pepper.proc.proc :as-alias p]
            [pepper.proc.xforms :as xf]))

(def queue (chan 1 xf/logging-xf))
(def by-topic (pub queue ::p/message-type))