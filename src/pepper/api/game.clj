(ns pepper.api.game
  "See https://javabwapi.github.io/JBWAPI/bwapi/Game.html"
  (:require [clojure.java.data :as j]
            [clojure.reflect :as r])
  (:import (bwapi Game Text)))

(def omit?
  "Game properties I'll probably want to omit
   - TODO: interop whitelisting (right now it's a blacklist)"
  #{:staticMinerals
    :staticGeysers
    :neutralUnits
    :staticNeutralUnits
    :players
    #_:allUnits
    :minerals
    :startLocations
    :allRegions
    :forces
    :screenPosition
    :mousePosition
    :latency
    :geysers
    :gameType})

(def properties
  [:battleNet
   :bullets
   :averageFPS
   :paused
   :APM
   :allUnits
   :minerals
   :forces
   :staticGeysers
   :instanceNumber
   :revision
   :frameCount
   :staticNeutralUnits
   :inGame
   :FPS
   :allRegions
   :remainingLatencyTime
   :latComEnabled
   :multiplayer
   :mousePosition
   :replay
   :startLocations
   :gameType
   :debug
   :replayFrameCount
   :nukeDots
   :randomSeed
   :selectedUnits
   :latencyFrames
   :latencyTime
   :lastEventTime
   :latency
   :geysers
   :players
   :staticMinerals
   :remainingLatencyFrames
   :neutralUnits
   :screenPosition])

(def keywords
  [:battle-net
   :bullets
   :average-fps
   :paused
   :apm
   :all-units
   :minerals
   :forces
   :static-geysers
   :instance-number
   :revision
   :frame-count
   :static-neutral-units
   :in-game
   :fps
   :all-regions
   :remaining-latency-time
   :lat-com-enabled
   :multiplayer
   :mouse-position
   :replay
   :start-locations
   :game-type
   :debug
   :replay-frame-count
   :nuke-dots
   :random-seed
   :selected-units
   :latency-frames
   :latency-time
   :last-event-time
   :latency
   :geysers
   :players
   :static-minerals
   :remaining-latency-frames
   :neutral-units
   :screen-position])

;;;;

(defn with-game
  "Wraps game functions so that they can be passed without game and then called with game from somewhere else. 
   
   eg:
   1. ns (a) outputs `(with-game game/set-local-speed :fastest)` 
   2. ns (b) receives output `f` and does `(f game))`
   
   - TODO: why not datafy this? such as...
   ```
   {:fn set-game-speed
   :params [:fastest]}
   ```"
  [f & args]
  (fn [game]
    (apply f game args)))

;;;;

(defn get-frame-count [game]
  (.getFrameCount game))

(defn draw-text-screen
  [game x y text]
  (.drawTextScreen game x y text (into-array Text [])))

(defn pause-game
  [game]
  (.pauseGame game))

(defn set-local-speed
  [game speed]
  (.setLocalSpeed game (speed {:fastest 42
                               :faster 48
                               :fast 56
                               :normal 67
                               :slow 83
                               :slower 111
                               :slowest 167}
                              42)))

(defn self
  "Retrieves the player object that BWAPI is controlling."
  [game]
  (.self game))

(defn can-make
  "Checks all the requirements in order to make a given unit type for the current player. 
   These include resources, supply, technology tree, availability, and required units."
  ([game unit-type]
   (.canMake game unit-type))

  ([game unit-type unit]
   (.canMake game unit-type unit)))

(defn get-build-location
  "Retrieves a basic build position just as the default Computer AI would.
   
   - tile-position - A valid TilePosition containing the desired placement position.
   - max-range - The maximum distance (in tiles) to build from desiredPosition."
  ([game unit-type tile-position]
   (.getBuildLocation game unit-type tile-position))

  ([game unit-type tile-position max-range]
   (.getBuildLocation game unit-type tile-position max-range)))

(defn send-text
  "Sends a text message to all other players in the game. 
   
   In a single player game this function can be used to execute cheat codes."
  [game text]
  (.sendText game text (into-array Text [])))

(defn get-unit [game unit-id]
  (.getUnit game unit-id))

(defn get-minerals
  [game]
  (.getMinerals game))

(defn get-geysers [game]
  (.getGeysers game))

(defn get-players [game]
  (.getPlayers game))

(defn leave-game
  "Leaves the current game by surrendering and enters the post-game statistics/score screen."
  [game]
  (.leaveGame game))

(defn is-in-game
  [game]
  (.isInGame game))

(defn is-paused
  [game]
  (.isPaused game))

(defn resume-game
  [game]
  (.resumeGame game))

(defn get-all-units
  [game]
  (.getAllUnits game))

(defn get-start-locations
  [game]
  (.getStartLocations game))

(defn elapsed-time
  [game]
  (.elapsedTime game))

;;;; more junk to organize

#_(defn routes []
    {:game {:get-all-units {:handler (partial (fn [game]
                                                :get-all-units))}}})

(defn routes []
  {:game {:get-all-units {:handler (fn [game] (.getAllUnits game))}}})

(defn unit [unit]
  {:id (.getID unit)})

(defn mock-dispatcher [id mock]
  (case id
    :get-all-units mock))

(defn game-dispatcher [id game]
  (case id
    :get-all-units (->> (.getAllUnits game)
                        (map unit))))

(defn game-handler [id]
  (case id
    :get-all-units []
    :elapsed-time 2))