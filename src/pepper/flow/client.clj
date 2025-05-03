(ns pepper.flow.client
  (:require
   [pepper.client :as client]
   [pepper.bwapi.game :as g]
   [clojure.core.async :as a]
   [clojure.spec.alpha :as s]
   [clojure.core.async.flow :as flow]))

;; in-ports
(s/def ::in-event any?)

;; outs
(s/def ::out-event any?)

(defn proc
  "TODO: pausing and stopping"

  ([] {:outs {::out-event "game events"}})

  ;; init
  ([args]
   (assoc args
          ::flow/in-ports
          {::in-event (a/chan (a/sliding-buffer 1))}))

  ;; transition
  ([{:keys [client game-future ::flow/in-ports] :as state} transition]
   (case transition
     ::flow/resume
     (let [in-port (::in-event in-ports)
           client (client/init! (partial a/put! in-port))
           game-future (future (client/start-game! client))]
       (assoc state :client client :game-future game-future))

     ::flow/pause
     (println "haha")

     ::flow/stop
     (let [game (client/get-game client)]
       (println "haha")
       (g/leave-game game)
       (println "left the game?" (g/is-in-game game)))))

  ([{:keys [client] :as state} input message]
   (case input
     ::in-event
     (case (:event message)
       :on-start [state {::out-event [(assoc message :client client)]
                         #_::flow/report #_[{:message message
                                             :inst (java.util.Date.)}]}]
       [state {::out-event [message]
               #_::flow/report #_[{:message message :inst (java.util.Date.)}]}]))))