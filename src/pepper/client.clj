(ns pepper.client
  (:import (bwapi BWEventListener BWClient)))

(defn event-listener [f]
  (reify BWEventListener

    (onEnd [this isWinner]
      (f {:event :on-end
          :data {:is-winner isWinner}}))

    (onFrame [this]
      (f {:event :on-frame}))

    (onNukeDetect [this target]
      (f {:event :on-nuke-detect
          :data {:target target}}))

    (onPlayerDropped [this player]
      (f {:event :on-player-dropped
          :data {:player player}}))

    (onPlayerLeft [this player]
      (f {:event :on-player-left
          :data {:player player}}))

    (onReceiveText [this player text]
      (f {:event :on-receive-text
          :data {:player player :text text}}))

    (onSaveGame [this gameName]
      (f {:event :on-save-game
          :data {:gameName gameName}}))

    (onSendText [this text]
      (f {:event :on-send-text
          :data {:text text}}))

    (onStart [this]
      (f {:event :on-start}))

    (onUnitComplete [this unit]
      (f {:event :on-unit-complete
          :data {:unit unit}}))

    (onUnitCreate [this unit]
      (f {:event :on-unit-create
          :data {:unit unit}}))

    (onUnitDestroy [this unit]
      (f {:event :on-unit-destroy
          :data {:unit unit}}))

    (onUnitDiscover [this unit]
      (f {:event :on-unit-discover
          :data {:unit unit}}))

    (onUnitEvade [this unit]
      (f {:event :on-unit-evade
          :data {:unit unit}}))

    (onUnitHide [this unit]
      (f {:event :on-unit-hide
          :data {:unit unit}}))

    (onUnitMorph [this unit]
      (f {:event :on-unit-morph
          :data {:unit unit}}))

    (onUnitRenegade [this unit]
      (f {:event :on-unit-renegade
          :data {:unit unit}}))

    (onUnitShow [this unit]
      (f {:event :on-unit-show
          :data {:unit unit}}))))

(defn start!
  "Do I want to pass a channel, or create a channel here?"
  [f]
  {:client (BWClient.
            (event-listener
             (fn callback [e] (f e))))})

(defn start-game
  [client]
  (.startGame client))

(defn get-game
  [client]
  (.getGame client))