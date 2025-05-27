(ns pepper.procs.asker
  (:require
   [pepper.api.game :as game]))

(defn proc
  ([] {:ins {:trigger "will trigger a question"
             :response "question response"}
       :outs {:question "asks a question"}})
  ([args] args)
  ([state transition] state)
  ([state input-id input]
   (case input-id
     :trigger [state {:question [{:pid :asker
                                  :inid :response
                                  :request (game/with-game game/draw-text-screen
                                             100 100 (str (inst-ms (java.time.Instant/now))))}]}]
     :response [state nil])))