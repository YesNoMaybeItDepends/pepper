(ns pepper.core
  (:require
   [clojure.core.async :as a]
   [clojure.core.async.flow :as flow]
   [clojure.core.async.flow-monitor :as flow-monitor]
   [pepper.api.client :as client]
   [pepper.api.game :as g]
   [pepper.procs.handler :as handler]
   [pepper.procs.game :as game]
   [pepper.utils.chaoslauncher :as chaoslauncher]
   [pepper.utils.repl :as repl]
   [taoensso.telemere :as t]
   [pepper.utils.portal :as portal]))

(defn init-logging []
  (t/add-handler! :file-handler (t/handler:file {:path ".log"
                                                 :output-fn (t/pr-signal-fn {:pr-fn :edn})})))

(init-logging)

(defonce state (atom nil))
(defonce flow-monitor false)

(defn create-flow
  [from-game to-game game]
  (flow/create-flow {:procs {:handler {:proc (flow/process #'handler/proc)
                                       :args {:game game
                                              :from-game from-game
                                              :to-game to-game}}
                             :game {:proc (flow/process #'game/proc)
                                    :args {:game game}}}
                     :conns [[[:handler :out] [:game :game-event]]]}))

(defn init-flow-monitor
  "TODO: flow-monitor throws error"
  [_flow]
  (when (true? flow-monitor)
    (alter-var-root #'flow-monitor (fn [x] (flow-monitor/start-server _flow)))))

(defn init-portal
  []
  (when (true? true)
    (portal/start!)))

(defn init-flow
  [from-game to-game game]
  (let [_flow (create-flow from-game to-game game)
        channels (flow/start _flow)
        portal (init-portal)]
    (swap! state assoc
           :flow _flow
           :report-chan (:report-chan channels)
           :error-chan (:error-chan channels)
           :portal portal)
    (init-flow-monitor _flow)
    (flow/resume _flow)
    true))

(defn on-start-hook [game]
  (g/pause-game game)
  (g/set-local-speed game :slowest))

(defn handle-on-start [event from-game to-game]
  (let [repl (repl/start-server!)
        client (:client @state)
        game (client/get-game client)]
    (#'on-start-hook game)
    (init-flow from-game to-game game)))

(defn handle-on-frame
  [event from-game to-game]
  (let [to-flow (a/>!! from-game event)
        from-flow (a/<!! to-game)]
    from-flow))

(defn handle-on-end
  "TODO: The game should now be over and I should shut down now"
  [event from-game to-game]
  (t/event! :handle-on-end-start)
  (let [state @state
        flow (:flow state)
        portal (:portal state)
        #_repl #_(:repl state)]
    (flow/stop flow)
    (repl/stop-server!)
    (chaoslauncher/stop!)
    (portal/stop! portal)
    (Thread/sleep 1000)
    (t/event! :handle-on-end-shutting-down)
    (shutdown-agents)))

(defn event-handler
  [from-game to-game {:keys [event] :as _event}]
  (t/event!? {:when (some? (#{:on-start :on-end} event))
              :data event})
  (case event
    :on-start (#'handle-on-start _event from-game to-game)
    :on-frame (#'handle-on-frame _event from-game to-game)
    :on-end (#'handle-on-end _event from-game to-game)
    nil))

(defn -main
  []
  (t/event! :main-start)
  (let [from-game (a/chan (a/sliding-buffer 10))
        to-game (a/chan (a/sliding-buffer 10))
        client (client/make-client (partial #'event-handler from-game to-game))]
    (swap! state assoc
           :client client
           :from-game from-game
           :to-game to-game)
    (chaoslauncher/start!)
    (client/start-game client {:async false
                               :debug-connection false
                               :log-verbosely false}))
  (t/event! :main-shutting-down)
  (shutdown-agents))

