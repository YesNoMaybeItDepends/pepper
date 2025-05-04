(ns pepper.core
  (:require
   [clojure.core.async :as a]
   [clojure.spec.alpha :as s]
   [pepper.starcraft :as starcraft]
   [clojure.core.async.flow :as flow]
   [pepper.flow.client :as client]
   [pepper.flow.printer :as printer]
   [pepper.flow.game :as game]
   [clojure.core.async.flow-monitor :as mon]))

(s/check-asserts true) ;; TODO: keep here or elsewhere

(defonce state
  (atom {:flow nil
         :report-chan nil
         :error-chan nil}))

(defonce monitor-server (atom nil))

(defn init! [state]
  (assoc state :flow (flow/create-flow
                      {:procs {:client {:proc (flow/process #'client/proc)}
                               :printer {:proc (flow/process #'printer/proc)}
                               :game {:proc (flow/process #'game/proc)}}

                       :conns [[[:client ::client/out-event] [:game ::game/in-event]]
                               [[:game ::game/out]  [:printer :in]]]})))

(defn start! [state]
  (merge state (flow/start (:flow state))))

(defn pause! []
  (flow/pause (:flow @state)))

(defn resume! []
  (flow/resume (:flow @state)))

(defn stop! []
  (flow/stop (:flow @state))
  (starcraft/stop!)
  (mon/stop-server monitor-server))

((fn auto-init []
   (when ((fn auto-init? [] true))
     (when (nil? (:flow @state))
       (swap! state init!)
       (when (nil? @monitor-server)
         (def monitor-server (mon/start-server @state)))))))

((fn auto-start []
   (when ((fn auto-run? [] false))
     (do ;; eval-manually
       (swap! state start!)
       (flow/resume (:flow @state))
       (starcraft/start!)))))

(comment

  (a/poll! (:report-chan @state))
  (a/poll! (:error-chan @state))

  @state
  @monitor-server

  #_())

;; misc 
(defn java-game->game-data [game]
  (let [game (bean game)]
    (keys game)))