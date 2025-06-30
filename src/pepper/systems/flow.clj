(ns pepper.systems.flow
  (:require
   [clojure.core.async :as a]
   [clojure.core.async.flow :as flow]
   [pepper.api.client :as client]
   [pepper.api.game :as g]
   [pepper.procs.game :as game]
   [pepper.procs.handler :as handler]
   [pepper.systems.repl :as repl]
   [pepper.utils.chaoslauncher :as chaoslauncher]
   [taoensso.telemere :as t]
   [pepper.systems.flow-monitor :as pepper-flow-monitor]))

;;;; init

(defn create-flow
  [from-game to-game game]
  (flow/create-flow {:procs {:handler {:proc (flow/process #'handler/proc)
                                       :args {:game game
                                              :from-game from-game
                                              :to-game to-game}}
                             :game {:proc (flow/process #'game/proc)
                                    :args {:game game}}}
                     :conns [[[:handler :out] [:game :game-event]]]}))

(defn init-flow
  [store from-game to-game game]
  (let [_flow (create-flow from-game to-game game)
        channels (flow/start _flow)]
    (swap! store assoc
           :flow _flow
           :report-chan (:report-chan channels)
           :error-chan (:error-chan channels))
    (pepper-flow-monitor/init-flow-monitor _flow)
    (flow/resume _flow)
    true))

;;;; bwapi handlers

(defn on-start-hook [game]
  (g/pause-game game)
  (g/set-local-speed game :slowest))

(defn handle-on-start [store event from-game to-game]
  (let [repl (repl/start-server!)
        client (:client @store)
        game (client/get-game client)]
    (#'on-start-hook game)
    (init-flow store from-game to-game game)))

(defn handle-on-frame
  [store event from-game to-game]
  (let [to-flow (a/>!! from-game event)
        from-flow (a/<!! to-game)]
    from-flow))

(defn handle-on-end
  "TODO: The game should now be over and I should shut down now"
  [store event from-game to-game]
  (t/event! :handle-on-end-start)
  (let [state @store
        flow (:flow state)
        #_repl #_(:repl state)]
    (flow/stop flow)
    (repl/stop-server!)
    (chaoslauncher/stop!)
    (Thread/sleep 1000)
    (t/event! :handle-on-end-shutting-down)
    (shutdown-agents)))

(defn event-handler
  [store from-game to-game {:keys [event] :as e}]
  (t/event!? {:when (some? (#{:on-start :on-end} event))
              :data event})
  (case event
    :on-start (#'handle-on-start store e from-game to-game)
    :on-frame (#'handle-on-frame store e from-game to-game)
    :on-end (#'handle-on-end store e from-game to-game)
    nil))

;;;; main

(defn flow-main [store]
  (let [from-game (a/chan (a/sliding-buffer 10))
        to-game (a/chan (a/sliding-buffer 10))
        client (client/make-client (partial #'event-handler store from-game to-game))]
    (swap! store assoc
           :client client
           :from-game from-game
           :to-game to-game)
    (chaoslauncher/start!)
    (client/start-game client {:async false
                               :debug-connection false
                               :log-verbosely false})))