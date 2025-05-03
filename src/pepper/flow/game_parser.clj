(ns pepper.flow.game-parser
  (:require
   [clojure.spec.alpha :as s]
   [pepper.bwapi.game :as g]
   [pepper.client :as client]))

;; in
(s/def ::in-event any?)

;; out
(s/def ::out any?)

(defn parse-game-state [state game]
  (if game (do #_(g/send-text game "test")
            (assoc state
                   :frame (.getFrameCount game)
                   :paused (.isPaused game)))
      state))

(defn proc
  "Parses the game state on frame"

  ;; describe
  ([] {:ins {::in-event "event"}

       :outs {::out "output"}})

  ;; init
  ([args] (assoc args
                 :client nil
                 :game nil
                 :game-state {}))

  ;; transition
  ([state transition] state)

  ;; transform
  ([{:keys [client game-state game] :as state} input msg]
   (case input
     ::in-event (case (:event msg)
                  :on-start (let [_test (println "game started")
                                  client (:client msg)
                                  game (client/get-game client)
                                  game-state (parse-game-state game-state game)]
                              [(assoc state
                                      :client client
                                      :game game
                                      :game-state game-state)
                               {::out [(:frame game-state)]}])

                  ;; anything else
                  (if client (let [#_game #_(client/get-game client)
                                   game-state (parse-game-state game-state game)]
                               [(assoc state
                                       :game-state game-state)
                                {::out [(:frame game-state)]}])

                      [state {::out ["There's no client"]}])))))
