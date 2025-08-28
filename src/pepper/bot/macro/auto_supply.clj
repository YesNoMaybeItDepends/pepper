(ns pepper.bot.macro.auto-supply
  (:require
   [pepper.bot.jobs.build :as build]
   [pepper.game.resources :as resources]
   [pepper.game.unit-type :as unit-type]
   [pepper.bot.our :as our]))

(def minimum-available-supply 8)

(defn under-minimum-supply? [supply]
  (<= (resources/supply->supply-available supply)
      minimum-available-supply))

(defn need-supply? [our game]
  (let [supply (our/supply our game)
        supply-capped? (resources/supply-capped? supply)
        under-minimum-available-supply? (under-minimum-supply? supply)]
    (and (not supply-capped?) under-minimum-available-supply?)))

(defn can-afford? [our game]
  (resources/can-afford? (our/resources-available our game)
                         (unit-type/cost :supply-depot)))

(defn building-supply? [unit-jobs]
  (first (filterv #(= (build/building %) :supply-depot) unit-jobs)))