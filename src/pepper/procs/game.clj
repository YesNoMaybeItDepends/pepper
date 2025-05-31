(ns pepper.procs.game
  (:require
   [pepper.procs.proc :as proc]
   [pepper.api.game :as game]
   [taoensso.telemere :as tel]))

(defn game-event-handler
  [state {:keys [event] :as in}]
  (case event
    :on-frame (do (when (:on-start? state) (println "on-start?" (:on-start? state))) [state nil])
    :on-start (do (println "!!!!!!!!!! on start !!!!!!!!!!!!!!!!!") [(assoc state :on-start? true) nil])
    [state nil]))

(defn proc
  ([] {:params {:game "bwapi game"}
       :ins {:game-event nil}})
  ([args]
   (let [state (assoc (proc/init-state args proc)
                      :on-start? true
                      :game-state (game/get-frame-count (:game args)))]
     state))
  ([state lifecycle] state)
  ([state inid in]
   (case inid
     :game-event (#'game-event-handler state in)
     [state nil])))