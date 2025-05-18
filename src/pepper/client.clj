(ns pepper.client
  "TODO: move to bwapi"
  (:import (bwapi BWEventListener BWClient BWClientConfiguration)))

(defn client-configuration
  ([] (new BWClientConfiguration))
  ([{:keys [async
            async-frame-buffer-capacity
            async-unsafe
            auto-continue
            debug-connection
            log-verbosely
            max-frame-duration-ms
            unlimited-frame-zero]
     :as config}]

   (cond-> (new BWClientConfiguration)
     (some? async) (.withAsync async)
     (some? async-frame-buffer-capacity) (.withAsyncFrameBufferCapacity async-frame-buffer-capacity)
     (some? async-unsafe) (.withAsyncUnsafe async-unsafe)
     (some? auto-continue) (.withAutoContinue auto-continue)
     (some? debug-connection) (.withDebugConnection debug-connection)
     (some? log-verbosely) (.withLogVerbosely log-verbosely)
     (some? max-frame-duration-ms) (.withMaxFrameDurationMs max-frame-duration-ms)
     (some? unlimited-frame-zero) (.withUnlimitedFrameZero unlimited-frame-zero))))

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
      (do (tap> "client on-start event")
          (f {:event :on-start})))

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

(defn client
  [f]
  (BWClient.
   (event-listener
    (fn callback [e] (f e)))))

(defn start-game!
  ([client] (.startGame client))
  ([client config] (.startGame client config)))

(defn get-game
  [client]
  (.getGame client))