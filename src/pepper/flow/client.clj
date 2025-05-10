(ns pepper.flow.client
  (:require
   [pepper.client :as client]
   [pepper.api.game :as game]
   [clojure.core.async :as a]
   [clojure.spec.alpha :as s]
   [clojure.core.async.flow :as flow]
   [pepper.starcraft :as starcraft]
   [pepper.api.bwem :as bwem]))

;; in-ports
(s/def ::bwapi-events any?)
(s/def ::starcraft-started any?)
(s/def ::client-started any?)
(s/def ::client-created any?)
(s/def ::game-started any?)
(defn in-ports [] {::bwapi-events      (a/chan (a/sliding-buffer 1))
                   ::client-created    (a/chan)
                   ::client-started    (a/chan)
                   ::starcraft-started (a/chan)
                   ::game-started      (a/chan)})

;; outs
(s/def ::out-event any?)
#_(s/def ::event #{:on-start :on-frame :something-else})

(defn proc
  "TODO: pausing and stopping"

  ([] {:outs {::out-event "game events"}})

  ;; init
  ([args]
   (let [in-ports (in-ports)
         client (client/client (partial a/put! (::bwapi-events in-ports)))]
     (assoc args :client client ::flow/in-ports in-ports)))

  ;; transition
  ([{:keys [client] :as state} transition]
   (case transition
     ::flow/resume
     (assoc state
            :game-future (future (client/start-game! client))
            :starcraft-future (future
                                (Thread/sleep 1000)
                                (starcraft/start!)))

     ::flow/pause
     (println "haha")

     ::flow/stop
     (do
       (println "haha")
       (game/leave-game)
       (println "left the game?" (game/is-in-game)))))

  ([{:keys [client] :as state} input message]
   (case input
     ::bwapi-events (case (:event message)
                      :on-start (let [game (client/get-game client)
                                      bwem (bwem/init) #_"implicitly takes game"]
                                  (game/bind-game! game)
                                  (bwem/bind-bwem! bwem)
                                  [state {::out-event [message]}])
                      [state {::out-event [message]}]))))