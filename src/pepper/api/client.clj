(ns pepper.api.client
  (:require [babashka.process :as p])
  (:import (bwapi BWClient BWClientConfiguration BWEventListener)))

(def event->msg
  "map of api event to the shape of the event message to be sent"
  {:on-start [:on-start nil]
   :on-end [:on-end {:is-winner "isWinner"}]
   :on-frame [:on-frame nil]
   :on-save-game [:on-save-game {:gameName "gameName"}]
   :on-send-text [:on-send-text {:text "text"}]
   :on-receive-text [:on-receive-text {:player "player" :text "text"}]
   :on-player-left [:on-player-left {:player "player"}]
   :on-player-dropped [:on-player-dropped {:player "player"}]
   :on-nuke-detect [:on-nuke-detect {:position "position"}]
   :on-unit-complete [:on-unit-complete {:unit "unit"}]
   :on-unit-create [:on-unit-create {:unit "unit"}]
   :on-unit-destroy [:on-unit-destroy {:unit "unit"}]
   :on-unit-discover [:on-unit-discover {:unit "unit"}]
   :on-unit-evade [:on-unit-evade {:unit "unit"}]
   :on-unit-hide [:on-unit-hide {:unit "unit"}]
   :on-unit-morph [:on-unit-morph {:unit "unit"}]
   :on-unit-renegade [:on-unit-renegade {:unit "unit"}]
   :on-unit-show [:on-unit-show {:unit "unit"}]})

(defn make-configuration
  ([] (BWClientConfiguration.))
  ([{:keys [async
            async-frame-buffer-capacity
            async-unsafe
            auto-continue
            debug-connection
            log-verbosely
            max-frame-duration-ms
            unlimited-frame-zero]
     :as config}]

   (cond-> (BWClientConfiguration.)
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
      (f [:on-end {:is-winner isWinner}]))

    (onFrame [this]
      (f [:on-frame]))

    (onNukeDetect [this position]
      (f [:on-nuke-detect {:position position}]))

    (onPlayerDropped [this player]
      (f [:on-player-dropped {:player player}]))

    (onPlayerLeft [this player]
      (f [:on-player-left {:player player}]))

    (onReceiveText [this player text]
      (f [:on-receive-text {:player player :text text}]))

    (onSaveGame [this gameName]
      (f [:on-save-game {:gameName gameName}]))

    (onSendText [this text]
      (f [:on-send-text {:text text}]))

    (onStart [this]
      (f [:on-start]))

    (onUnitComplete [this unit]
      (f [:on-unit-complete {:unit unit}]))

    (onUnitCreate [this unit]
      (f [:on-unit-create {:unit unit}]))

    (onUnitDestroy [this unit]
      (f [:on-unit-destroy {:unit unit}]))

    (onUnitDiscover [this unit]
      (f [:on-unit-discover {:unit unit}]))

    (onUnitEvade [this unit]
      (f [:on-unit-evade {:unit unit}]))

    (onUnitHide [this unit]
      (f [:on-unit-hide {:unit unit}]))

    (onUnitMorph [this unit]
      (f [:on-unit-morph {:unit unit}]))

    (onUnitRenegade [this unit]
      (f [:on-unit-renegade {:unit unit}]))

    (onUnitShow [this unit]
      (f [:on-unit-show {:unit unit}]))))

(defn make-client
  [handler-fn]
  (BWClient.
   (event-listener handler-fn)))

(defn start-game!
  "Starts the game"
  ([client] (.startGame client))
  ([client config] (.startGame client (make-configuration config))))

;; this should actually be in dev

(defn run-starcraft!
  "Runs Starcraft by running Chaoslauncher.exe
   
   Requires the 'Run Starcraft on Startup' Chaoslauncher setting enabled."
  [chaos-launcher-path]
  (p/process chaos-launcher-path))

(defn kill-starcraft!
  "Kills both Starcraft and Chaoslauncher"
  []
  (p/sh "pskill starcraft")
  (p/sh "pskill chaoslauncher"))

(defn chaos-launcher-path [config]
  (get-in config [:starcraft :path]))