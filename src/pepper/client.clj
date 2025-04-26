(ns pepper.client
  (:require [pepper.events :as e]
            [pepper.proc.queue :as q]
            [clojure.core.async :refer [>!!]]
            [pepper.starcraft :as starcraft])
  (:import (bwapi BWEventListener BWClient)))

(defn to-queue "im doing something with this" [client-ref this]
  (>!! q/queue {:client client-ref
                :this this}))

(defn event-handler
  [client-ref event args]
  (#'e/event-handler client-ref event args))

(defn event-listener
  [client-ref meme-event-handler]
  (reify BWEventListener
    (onEnd [this isWinner]
      (#'e/event-handler
       {:client client-ref
        :event :on-end
        :isWinner isWinner}))

    (onFrame [this]
      (#'e/event-handler
       {:client client-ref
        :event :on-frame}))

    (onNukeDetect [this target]
      (#'e/event-handler
       {:client client-ref
        :event :on-nuke-detect
        :target target}))


    (onPlayerDropped [this player]
      (#'e/event-handler
       {:client client-ref
        :event :on-player-dropped
        :player player}))

    (onPlayerLeft [this player]
      (#'e/event-handler
       {:client client-ref
        :event :on-player-left
        :player player}))

    (onReceiveText [this player text]
      (#'e/event-handler
       {:client client-ref
        :event :on-receive-text
        :player player :text text}))

    (onSaveGame [this gameName]
      (#'e/event-handler
       {:client client-ref
        :event :on-save-game
        :gameName gameName}))

    (onSendText [this text]
      (#'e/event-handler
       {:client client-ref
        :event :on-send-text
        :text text}))

    (onStart [this]
      (#'e/event-handler
       {:client client-ref
        :event :on-start}))

    (onUnitComplete [this unit]
      (#'e/event-handler
       {:client client-ref
        :event :on-unit-complete
        :unit unit}))

    (onUnitCreate [this unit]
      (#'e/event-handler
       {:client client-ref
        :event :on-unit-create
        :unit unit}))

    (onUnitDestroy [this unit]
      (#'e/event-handler
       {:client client-ref
        :event :on-unit-destroy
        :unit unit}))

    (onUnitDiscover [this unit]
      (#'e/event-handler
       {:client client-ref
        :event :on-unit-discover
        :unit unit}))

    (onUnitEvade [this unit]
      (#'e/event-handler
       {:client client-ref
        :event :on-unit-evade
        :unit unit}))

    (onUnitHide [this unit]
      (#'e/event-handler
       {:client client-ref
        :event :on-unit-hide
        :unit unit}))

    (onUnitMorph [this unit]
      (#'e/event-handler
       {:client client-ref
        :event :on-unit-morph
        :unit unit}))

    (onUnitRenegade [this unit]
      (#'e/event-handler
       {:client client-ref
        :event :on-unit-renegade
        :unit unit}))

    (onUnitShow [this unit]
      (#'e/event-handler
       {:client client-ref
        :event :on-unit-show
        :unit unit}))))

(defn start!
  [client-ref event-handler]
  (let [client (BWClient. (event-listener client-ref event-handler))
        starcraft (starcraft/start!)]
    client))

(defn stop!
  [client]
  (.leaveGame (.getGame client))
  (starcraft/stop!)
  nil)

(defn start-game
  [client]
  (.startGame client))

(defn stop-game
  [client]
  (.leaveGame (.getGame client)))

(defn get-game
  [client]
  (.getGame client))