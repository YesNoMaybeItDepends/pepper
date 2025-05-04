(ns pepper.bwapi.impl.game
  "See https://javabwapi.github.io/JBWAPI/bwapi/Game.html"
  (:import (bwapi Text)))

(defonce game nil)
(defn bind-game! [g] (alter-var-root #'game (constantly g)))

(defn self
  "Retrieves the player object that BWAPI is controlling."
  []
  {:pre [(some? game)]}
  (.self game))

(defn can-make
  "Checks all the requirements in order to make a given unit type for the current player. 
   These include resources, supply, technology tree, availability, and required units."
  ([unit-type]
   {:pre [(some? game)
          (some? unit-type)]}
   (.canMake game unit-type))

  ([unit-type unit]
   {:pre [(some? game)
          (some? unit-type)
          (some? unit)]}
   (.canMake unit-type unit)))

(defn get-build-location
  "Retrieves a basic build position just as the default Computer AI would.
   
   - tile-position - A valid TilePosition containing the desired placement position.
   - max-range - The maximum distance (in tiles) to build from desiredPosition."
  ([unit-type tile-position]
   {:pre [(some? game)
          (some? unit-type)
          (some? tile-position)]}
   (.getBuildLocation game unit-type tile-position))

  ([unit-type tile-position max-range]
   {:pre [(some? game)
          (some? unit-type)
          (some? tile-position)
          (some? max-range)]}
   (.getBuildLocation game unit-type tile-position max-range)))

(defn send-text
  "Sends a text message to all other players in the game. 
   
   In a single player game this function can be used to execute cheat codes."
  [text]
  {:pre [(some? game)
         (some? text)]}
  (.sendText game text (into-array Text [])))

(defn draw-text-screen [x y text]
  {:pre [(some? game)
         (some? x)
         (some? y)
         (some? text)]}
  (.drawTextScreen game x y text (into-array Text [])))

(defn get-unit [unit-id]
  {:pre [(some? game)
         (some? unit-id)]}
  (.getUnit game unit-id))

(defn get-minerals []
  {:pre [(some? game)]}
  (.getMinerals game))

(defn get-geysers []
  {:pre [(some? game)]}
  (.getGeysers game))

(defn leave-game
  "Leaves the current game by surrendering and enters the post-game statistics/score screen."
  []
  {:pre [(some? game)]}
  (.leaveGame game))

(defn is-in-game
  []
  {:pre [(some? game)]}
  (.isInGame game))

(defn is-paused
  []
  {:pre [(some? game)]}
  (.isPaused game))

(defn get-frame-count
  []
  {:pre [(some? game)]}
  (.getFrameCount game))