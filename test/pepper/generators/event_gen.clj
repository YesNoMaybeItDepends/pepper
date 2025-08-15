(ns pepper.generators.event-gen
  (:require
   [clojure.test.check.generators :as gen]
   [pepper.api.client :as api]
   [pepper.game.position :as position]
   [pepper.generators.player-gen :as player-gen]
   [pepper.generators.position-gen :as position-gen]
   [pepper.generators.unit-gen :as unit-gen]
   [pepper.mocking :as mocking]))

(def arg-is-winner gen/boolean)

(def arg-game-name gen/string-alphanumeric)

(def arg-text gen/string-ascii)

(def arg-position (gen/fmap
                   (fn [pos] (position/->bwapi pos))
                   position-gen/position))

(def arg-player (gen/fmap
                 mocking/mock-player
                 player-gen/player))

(def arg-unit (gen/fmap
               mocking/mock-unit
               (unit-gen/unit)))

(def arg-event (gen/elements api/event-id->params))

(def event-args (gen/hash-map
                 :event arg-event
                 :is-winner arg-is-winner
                 :game-name arg-game-name
                 :text arg-text
                 :player arg-player
                 :position arg-position
                 :unit arg-unit))

(defn map-args [data]
  (let [[id params] (:event data)]
    [id (mapv
         (fn [param] (param data))
         params)]))

(def event (gen/fmap
            map-args
            event-args))