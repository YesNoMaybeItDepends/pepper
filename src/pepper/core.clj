(ns pepper.core
  (:require
   [clojure.core.async :as a]
   [clojure.spec.alpha :as s]
   [pepper.starcraft :as starcraft]
   [clojure.core.async.flow :as flow]
   [pepper.flow.client :as client]
   [pepper.flow.printer :as printer]
   [pepper.flow.game-state :as game-state]
   [clojure.core.async.flow-monitor :as mon]))

(s/check-asserts true) ;; TODO: keep here or elsewhere ? 

(defonce settings {:auto-init? true
                   :auto-start? false})

(defonce state
  (atom {:flow nil
         :report-chan nil
         :error-chan nil}))

(defonce monitor-server (atom nil))

(defn init! [state]
  (println "Initializing flow")
  (assoc state :flow (flow/create-flow
                      {:procs {:client {:proc (flow/process #'client/proc)}
                               :game-state {:proc (flow/process #'game-state/proc)}
                               :printer {:proc (flow/process #'printer/proc)}}

                       :conns [[[:client ::client/out-event] [:game-state ::game-state/in-event]]
                               [[:game-state ::game-state/out]  [:printer :in]]]})))

(defn start! []
  (println "Starting flow")
  (swap! state (fn [state] (merge state (flow/start (:flow state)))))
  (flow/resume (:flow @state)))

(defn pause! []
  (flow/pause (:flow @state)))

(defn resume! []
  (flow/resume (:flow @state)))

(defn stop! []
  (flow/stop (:flow @state))
  (starcraft/stop!)
  (mon/stop-server monitor-server))

(when (:auto-init? settings)
  (when (nil? (:flow @state))
    (swap! state init!)
    (when (nil? @monitor-server)
      (def monitor-server (mon/start-server @state)))))

(when (:auto-start? settings)
  (when (nil? (:report-chan @state))
    (start!)))

(comment

  (init! state)
  (start!)
  (pause!)
  (resume!)
  (stop!)

  (a/poll! (:report-chan @state))
  (a/poll! (:error-chan @state))

  settings
  @state
  (:flow @state)
  @monitor-server

  #_())

;; misc 
(defn java-game->game-data [game]
  (let [game (bean game)]
    (keys game)))