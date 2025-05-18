(ns pepper.procs.client
  (:require
   [clojure.core.async :as a]
   [clojure.core.async.flow :as flow]
   [clojure.spec.alpha :as s]
   [pepper.api.bwem :as bwem]
   [pepper.api.game :as game]
   [pepper.client :as client]
   [pepper.starcraft :as starcraft])
  (:import
   [bwapi Text]))

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
(s/def ::game-event any?)
#_(s/def ::event #{:on-start :on-frame :something-else})

(def out? #{::game-event})

(defn proc
  "TODO: pausing and stopping"

  ([] {:ins {::action "any kind of action with the game"}
       :outs {::game-event "game events"}})

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
            :game-future (client/start-game! client) #_(future (client/start-game! client))
            #_:starcraft-future #_(future
                                    (Thread/sleep 1000)
                                    (starcraft/start!)))

     ::flow/pause
     (println "haha")

     ::flow/stop
     (do
       (println "haha")
       (game/leave-game)
       (println "left the game?" (game/is-in-game)))))

  ([{:keys [client game] :as state} input message]
   (case input
     ::bwapi-events (case (:event message)
                      :on-start (let [game (.getGame client)]
                                  [(assoc state :game game) {::game-event [(assoc message :game game)]}])
                      (let [game (.getGame client)]
                        [(assoc state :game game) {::game-event [(assoc message :game game)]}])))))


#_(do (.drawTextScreen (.getGame client)
                       100
                       100
                       (-> client
                           .getGame
                           .self
                           .minerals
                           str)
                       (into-array Text []))
      [state {::game-event [message]}])