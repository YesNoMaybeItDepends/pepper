(ns pepper.proc.lab
  (:require
   [pepper.proc.proc :as p]
   [pepper.proc.queue :as queue]
   [pepper.proc.hello-world :as hw]
   [zprint.core :as zp]
   [clojure.core.async :as a :refer [>!! >! <! <!!]]))

(def AUTO-RUN (atom false))

(defonce procs
  (when @AUTO-RUN
    (atom {:hello-world
           (p/init {:proc hw/proc
                    :pub queue/by-topic
                    :output queue/queue})})))



(defn post-message [message] (>!! queue/queue message))