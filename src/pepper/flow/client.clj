(ns pepper.flow.client
  (:require
   [pepper.client :as client]
   [clojure.core.async :as a]
   [clojure.core.async.flow :as flow]))

(defn proc
  "Starts the client and the game.
   
   Once started, the internal port :events receives game events.

   Outputs these game events on its :out port.
   
   TODO: pausing and stopping"

  ([] {:outs {:out "game events"}})

  ([args]
   (assoc args
          ::flow/in-ports
          {:events (a/chan (a/sliding-buffer 1))}))

  ([{:keys [::flow/in-ports] :as state} transition]
   (case transition
     ::flow/resume
     (let [events (:events in-ports)
           client (client/start! (partial a/put! events))
           game (future (client/start-game (:client client)))]
       state)

     ::flow/pause
     (println "haha you cant pause...")

     ::flow/stop
     (println "haha you cant stop...")))

  ([state input message]
   [state (when (= input :events) {:out [message]})]))