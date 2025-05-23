(ns pepper.core
  (:require
   [clojure.spec.alpha :as s]
   [clojure.core.async :as a]
   #_[clojure.core.async.flow :as flow]
   #_[clojure.core.async.flow-monitor :as flow-mon]
   [pepper.api.client :as client]
   [pepper.api.game :as game]
   [pepper.utils.chaoslauncher :as chaoslauncher]
   [pepper.procs.printer :as printer]))

(defonce state (atom nil))

(defn on-frame [state e]
  (case (:event e)
    :on-start (swap! state assoc :game (client/get-game (:client @state)))
    :on-frame (when-let [g (:game @state)]
                (game/draw-text-screen (:game @state) 100 100 (str (game/get-frame-count g))))
    (println e)))

(defn event-handler [e] (#'on-frame state e))

(defn -main []
  (swap! state assoc :client (client/make-client #'event-handler))

  (let [game-future (future (client/start-game (:client @state) (client/make-configuration {:async false
                                                                                            :debug-connection true
                                                                                            :log-verbosely true}))
                            (println "Game finished, shutting down...")
                            (chaoslauncher/stop!)
                            (shutdown-agents))]
    (swap! state assoc :game-future game-future)
    (a/take! (a/timeout 1000) (chaoslauncher/start!))))