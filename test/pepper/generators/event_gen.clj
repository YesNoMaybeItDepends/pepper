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
                   (fn [pos] (position/->bwapi pos :position))
                   position-gen/position))

(def arg-player (gen/fmap
                 mocking/mock-player
                 player-gen/player))

(def arg-unit (gen/fmap
               mocking/mock-unit
               (unit-gen/unit)))

(def arg-event (gen/elements api/event-id->params))

(defn arg-events [events]
  (let [events (cond
                 (set? events) events
                 (seqable? events) (into #{} (flatten events))
                 :else #{events})]
    (gen/elements (reduce-kv (fn [m k v]
                               (if (contains? events k)
                                 (assoc m k v)
                                 m))
                             {}
                             api/event-id->params))))

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

(def on-start (gen/return [:on-start []]))
(def on-frame (gen/fmap
               map-args
               (gen/hash-map
                :event (arg-events :on-frame))))

(defn event-in [events]
  (gen/fmap
   map-args
   (gen/hash-map
    :event (arg-events events)
    :is-winner arg-is-winner
    :game-name arg-game-name
    :text arg-text
    :player arg-player
    :position arg-position
    :unit arg-unit)))

(comment

  (gen/sample (event-in [:on-start :on-frame :on-unit-create :on-unit-discover :on-unit-show :on-unit-complete]))

  #_())