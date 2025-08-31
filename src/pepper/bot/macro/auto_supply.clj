(ns pepper.bot.macro.auto-supply
  (:require
   [pepper.bot.jobs.build :as build]
   [pepper.bot.our :as our]
   [pepper.game.player :as player]
   [pepper.game.resources :as resources]
   [pepper.game.unit-type :as unit-type]))

(def minimum-available-supply 8)

(defn under-minimum-supply? [supply]
  (<= (resources/supply->supply-available supply)
      minimum-available-supply))

(defn need-supply? [our-player]
  (let [supply (player/supply our-player)
        supply-capped? (resources/supply-capped? supply)
        under-minimum-available-supply? (under-minimum-supply? supply)]
    (and (not supply-capped?) under-minimum-available-supply?)))