(ns pepper.procs.asker
  (:require
   [pepper.api.game :as game]
   [pepper.procs.proc :as proc]))

(defn proc
  ([] {:ins {:trigger "will trigger a question"
             :response "question response"}
       :outs {:question "asks a question"}})
  ([args] (proc/init-state args #'proc))
  ([state transition] state)
  ([state input-id input]
   #_(println input-id "->" input)
   (case input-id
     :trigger (let [req (game/with-game game/draw-text-screen
                          100 100 (str (inst-ms (java.time.Instant/now))))]
                [state {:question [{:request req}]}])

     #_[state {:question [{:pid (proc/state->pid state)
                           :inid :response
                           :request (game/with-game game/draw-text-screen
                                      100 100 (str (inst-ms (java.time.Instant/now))))}]}]
     #_:response #_[state nil])))