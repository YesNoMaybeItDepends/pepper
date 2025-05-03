(ns pepper.events
  (:require [pepper.bwapi.client :as client]
            [pepper.bwapi.impl.game :as game]
            [pepper.bwapi.player :as player]
            [pepper.macro :as macro]
            [pepper.debug.cheats :as cheats]
            [pepper.debug.logger :as logger]
            [pepper.jobs.build :as build-job]
            [pepper.jobs.gather :as gather-job]))

(defn unwrap-game-lol
  "lol"
  [args]
  (client/get-game (deref (deref (:client args)))))

(defn on-test-handler [args] (println "DEBUG: on-test-handler"))

(defn not-found [args] (when false (println (:event args) " not found")))

(defn on-start-handler [args]
  (let [game (unwrap-game-lol args)]
    (game/draw-text-screen game 100 100 "game started")))

(defn draw-message-crystals [game player]
  (let [name (player/get-name player)
        minerals (player/minerals player)]
    (game/draw-text-screen game 100 100 (str name " has " minerals " crystals"))))

(defn on-frame-handler [args]
  (let [game (unwrap-game-lol args)
        player (game/self game)]
    (draw-message-crystals game player)
    (macro/run game)))

(defn on-send-text-handler [args]
  (let [text (:text args)
        game (unwrap-game-lol args)]
    (logger/logger-handler game text)
    (cheats/cheat-handler game text)))

(defn on-receive-text-handler [args]
  (let [game (unwrap-game-lol args)]))

(defn on-unit-create [{unit :unit :as args}]
  (let [game (unwrap-game-lol args)]
    (build-job/on-unit-create {:game game :unit unit})))

(defn on-unit-complete [args]
  (let [game (unwrap-game-lol args)
        unit (:unit args)]
    (build-job/on-unit-complete {:game game :unit unit})
    (gather-job/on-unit-complete {:game game :unit unit})))

(defn on-end-handler [args]
  (let [isWinner (:isWinner args)]
    (throw (Exception. "test"))))

(def handlers {:on-start #'on-start-handler
               :on-frame #'on-frame-handler
               :on-send-text #'on-send-text-handler
               :on-receive-text #'on-receive-text-handler
               :on-test #'on-test-handler
               :on-unit-create #'on-unit-create
               :on-unit-complete #'on-unit-complete
               :on-end-handler #'on-end-handler})

(defn event-handler
  [args]
  ((get handlers (:event args) #'not-found) args))



