(ns pepper.api.game
  "See https://javabwapi.github.io/JBWAPI/bwapi/Game.html"
  (:require [clojure.java.data :as j]
            [clojure.reflect :as r])
  (:import (bwapi Text Game)))

(defn self
  "Retrieves the player object that BWAPI is controlling."
  [game]
  #_{:pre [(some? game)]}
  (.self game))

(def game-speed {:fastest 42
                 :faster 48
                 :fast 56
                 :normal 67
                 :slow 83
                 :slower 111
                 :slowest 167})

(defn set-local-speed
  [game kw]
  {:pre [(some? (kw game-speed))]}
  (.setLocalSpeed game (kw game-speed)))

(defn can-make
  "Checks all the requirements in order to make a given unit type for the current player. 
   These include resources, supply, technology tree, availability, and required units."
  ([game unit-type]
   #_{:pre [(some? game)
            (some? unit-type)]}
   (.canMake game unit-type))

  ([game unit-type unit]
   #_{:pre [(some? game)
            (some? unit-type)
            (some? unit)]}
   (.canMake game unit-type unit)))

(defn get-build-location
  "Retrieves a basic build position just as the default Computer AI would.
   
   - tile-position - A valid TilePosition containing the desired placement position.
   - max-range - The maximum distance (in tiles) to build from desiredPosition."
  ([game unit-type tile-position]
   #_{:pre [(some? game)
            (some? unit-type)
            (some? tile-position)]}
   (.getBuildLocation game unit-type tile-position))

  ([game unit-type tile-position max-range]
   #_{:pre [(some? game)
            (some? unit-type)
            (some? tile-position)
            (some? max-range)]}
   (.getBuildLocation game unit-type tile-position max-range)))

(defn send-text
  "Sends a text message to all other players in the game. 
   
   In a single player game this function can be used to execute cheat codes."
  [game text]
  #_{:pre [(some? game)
           (some? text)]}
  (.sendText game text (into-array Text [])))

(defn draw-text-screen
  [game x y text]
  #_{:pre [(some? game)
           (some? x)
           (some? y)
           (some? text)]}
  (.drawTextScreen game x y text (into-array Text [])))

(defn get-unit [game unit-id]
  #_{:pre [(some? game)
           (some? unit-id)]}
  (.getUnit game unit-id))

(defn get-minerals
  [game]
  #_{:pre [(some? game)]}
  (.getMinerals game))

(defn get-geysers [game]
  #_{:pre [(some? game)]}
  (.getGeysers game))

(defn get-players [game]
  #_{:pre [(some? game)]}
  (.getPlayers game))

(defn leave-game
  "Leaves the current game by surrendering and enters the post-game statistics/score screen."
  [game]
  #_{:pre [(some? game)]}
  (.leaveGame game))

(defn is-in-game
  [game]
  #_{:pre [(some? game)]}
  (.isInGame game))

(defn is-paused
  [game]
  #_{:pre [(some? game)]}
  (.isPaused game))

(defn get-frame-count
  [game]
  #_{:pre [(some? game)]}
  (.getFrameCount game))

(defn pause-game
  [game]
  (.pauseGame game))

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


(def omit?
  ;; "Game properties I'll probably want to omit"
  ;; "TODO: interop whitelisting (right now it's a blacklist)"
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
