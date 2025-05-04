(ns pepper.flow.client
  (:require
   [pepper.client :as client]
   [pepper.bwapi.impl.game :as game]
   [clojure.core.async :as a]
   [clojure.spec.alpha :as s]
   [clojure.core.async.flow :as flow]))

;; in-ports
(s/def ::in-event any?)

;; outs
(s/def ::out-event any?)
#_(s/def ::event #{:on-start :on-frame :something-else})

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
     (do
       (println "haha")
       (game/leave-game)
       (println "left the game?" (game/is-in-game)))))

  ([{:keys [client] :as state} input message]
   (case input ::in-event (case (:event message)
                            :on-start (let [game (client/get-game client)]
                                        (game/bind-game! game)
                                        [state {::out-event [message]}])
                            [state {::out-event [message]}]))))