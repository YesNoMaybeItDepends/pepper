(ns pepper.core
  (:require
   [clojure.core.async :as a]
   [clojure.core.async.flow :as flow]
   [clojure.core.async.flow-monitor :as flow-monitor]
   [pepper.api.client :as client]
   [pepper.procs.asker :as asker]
   [pepper.procs.handler :as handler]
   [pepper.utils.chaoslauncher :as chaoslauncher]
   [pepper.utils.repl :as repl]))

(defonce state (atom nil))
(defonce flow-monitor false)

(defn create-flow
  [from-game to-game game]
  (flow/create-flow {:procs {:handler {:proc (flow/process #'handler/proc)
                                       :args {:game game
                                              :from-game from-game
                                              :to-game to-game}}
                             :asker {:proc (flow/process #'asker/proc)}}
                     :conns [[[:handler :out] [:asker :trigger]]
                             [[:asker :question] [:handler :request]]]}))

(defn init-flow
  "TODO: flow-monitor throws error"
  [from-game to-game game]
  (let [f (create-flow from-game to-game game)
        chs (flow/start f)]
    (swap! state assoc
           :flow f
           :report-chan (:report-chan chs)
           :error-chan (:error-chan chs))
    (when (true? flow-monitor)
      (alter-var-root #'flow-monitor (fn [x] (flow-monitor/start-server f))))
    (flow/resume f)
    true))

(defn handle-on-start [event from-game to-game]
  (let [repl (repl/start-server!)
        client (:client @state)
        game (client/get-game client)]
    (init-flow from-game to-game game)))

(defn handle-on-frame
  [event from-game to-game]
  (let [p (a/>!! from-game event)
        t (a/<!! to-game)]
    (println t)
    t))

(defn handle-on-end
  [event from-game to-game]
  (println "TODO: The game should now be over and I should shut down now")
  (let [state @state
        flow (:flow state)
        #_repl #_(:repl state)]
    (println "Shutting down from event handler...")
    (flow/stop flow)
    (repl/stop-server!)
    (chaoslauncher/stop!)
    (Thread/sleep 1000)
    (shutdown-agents)))

(defn event-handler
  [from-game to-game event]
  (case (:event event)
    :on-start (#'handle-on-start event from-game to-game)
    :on-frame (#'handle-on-frame event from-game to-game)
    :on-end (#'handle-on-end event from-game to-game)
    (println event)))

(defn -main
  []
  (let [from-game (a/chan (a/sliding-buffer 10))
        to-game (a/chan (a/sliding-buffer 10))
        client (client/make-client (partial #'event-handler from-game to-game))]
    (swap! state assoc
           :client client
           :from-game from-game
           :to-game to-game)
    (chaoslauncher/start!)
    (client/start-game client {:async false
                               :debug-connection true
                               :log-verbosely true}))
  (println "Shutting down...")
  (shutdown-agents))