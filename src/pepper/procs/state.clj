(ns pepper.procs.state
  (:require
   [pepper.procs.proc :as proc]
   [pepper.api.game :as game]
   [taoensso.telemere :as tel]))

(defn game-event-handler
  [{:keys [game game-state] :as state} {:keys [event] :as in}]
  (case event
    :on-frame [state nil]
    :on-start [state nil]
    [state nil]))

(defn proc
  ([] {:params {:game "bwapi game"}
       :ins {:game-event nil}})
  ([{:keys [game] :as args}]
   (assoc args
          :game game
          :game-state {}))
  ([state lifecycle] state)
  ([state inid in]
   (case inid
     :game-event (#'game-event-handler state in)
     [state nil])))