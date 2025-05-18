(ns pepper.debug.cheats
  (:require
   [clojure.string :as str]
   [pepper.api.game :as game]
   [pepper.config :as config]))

(def cheats ;; the value should also include the string cheat
  {:food-for-thought "No Supply Limit"

   :show-me-the-money "10,000 Minerals and Gas"
   :whats-mine-is-mine "500 Minerals"
   :breathe-deep "500 Vespene Gas"

   :modify-the-phase-variance "Build anything"
   :operation-cwal "Build faster"

   :medieval-man "Research All Abilities"
   :something-for-nothing "Research All Upgrades"

   :power-overwhelming "Units are invincible"
   :the-gathering "Units use no energy"

   :black-sheep-wall "Map completely revealed"
   :war-aint-what-it-used-to-be "Map without fog of war"

   :game-over-man "Instant Loss"
   :there-is-no-cow-level "Instant Win"
   :staying-alive "Continue Playing After Win"})

(defn config-cheats []
  (get-in (config/config) [:pepper :cheats]))

(defn key->str [key]
  (str/replace (name key) #"-" " "))

#_(defn cheat [game]
    (doseq [cheat (config-cheats)]
      (game/send-text game (key->str (get cheats cheat)))))

#_(defn cheat-handler [text]
    (cheat))