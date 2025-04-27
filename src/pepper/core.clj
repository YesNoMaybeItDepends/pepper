(ns pepper.core
  (:require
   [clojure.core.async :as async :refer [<! chan sliding-buffer go-loop put!]]
   [clojure.spec.alpha :as s]
   [pepper.client :as client]
   [pepper.starcraft :as starcraft]))

(s/check-asserts true) ;; TODO: keep here or elsewhere

(def AUTO-RUN false)

(defonce state (atom nil))

(defn start? []
  (and (nil? @state) AUTO-RUN))

(defn handle-event [e]
  (println (java.util.Date.) "->" e))

(defn channel-handler []
  (go-loop []
    (when-some [e (<! (:events @state))]
      (#'handle-event e)
      (recur))))

(defn start! []
  (let [events (chan (sliding-buffer 1))
        client (client/start! (partial put! events))
        game (future (client/start-game client))]
    (reset! state {:client client
                   :events events
                   :game game
                   :proc (channel-handler)})))

(defn stop!
  "TODO: Doesn't really work,
   the jbwapi client tries to reconnect by design.
   
   An option is to throw an error which makes it
   stop trying to reconnect. But with my 
   reified implementation it doesn't work.
   
   Another idea is to go back to the gen-class implementation,
   like Korhal, and launch the client externally 
   as a different system process"

  []
  (starcraft/stop!)
  ;; nuke everything
  (shutdown-agents))

(when (start?)
  (start!))

(comment

  (stop!)

  #_())