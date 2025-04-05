(ns pepper.bwapi.game
  "See https://javabwapi.github.io/JBWAPI/bwapi/Game.html"
  (:import (bwapi Text)))

(defn self
  "Retrieves the player object that BWAPI is controlling."
  [game]
  {:pre [(some? game)]}
  (.self game))

(defn can-make
  "Checks all the requirements in order to make a given unit type for the current player. 
   These include resources, supply, technology tree, availability, and required units."
  ([game unit-type]
   {:pre [(some? game)
          (some? unit-type)]}
   (.canMake game unit-type))

  ([game unit-type unit]
   {:pre [(some? game)
          (some? unit-type)
          (some? unit)]}
   (.canMake game unit-type unit)))

(defn get-build-location
  "Retrieves a basic build position just as the default Computer AI would.
   
   - tile-position - A valid TilePosition containing the desired placement position.
   - max-range - The maximum distance (in tiles) to build from desiredPosition."
  ([game unit-type tile-position]
   {:pre [(some? game)
          (some? unit-type)
          (some? tile-position)]}
   (.getBuildLocation game unit-type tile-position))

  ([game unit-type tile-position max-range]
   {:pre [(some? game)
          (some? unit-type)
          (some? tile-position)
          (some? max-range)]}
   (.getBuildLocation game unit-type tile-position max-range)))

(defn send-text
  "Sends a text message to all other players in the game. 
   
   In a single player game this function can be used to execute cheat codes."
  [game text]
  {:pre [(some? game)
         (some? text)]}
  (.sendText game text (into-array Text [])))

(defn draw-text-screen [game x y text]
  {:pre [(some? game)
         (some? x)
         (some? y)
         (some? text)]}
  (.drawTextScreen game x y text (into-array Text [])))

(defn get-unit [game unit-id]
  {:pre [(some? game)
         (some? unit-id)]}
  (.getUnit game unit-id))

(defn get-minerals [game]
  {:pre [(some? game)]}
  (.getMinerals game))

(defn get-geysers [game]
  {:pre [(some? game)]}
  (.getGeysers game))

(defn leave-game
  "Leaves the current game by surrendering and enters the post-game statistics/score screen."
  [game]
  {:pre [(some? game)]}
  (.leaveGame game))




