(ns pepper.game.auto-supply
  (:require
   [pepper.game.jobs :as jobs]
   [pepper.game.jobs.build :as build]
   [pepper.game.resources :as resources]
   [pepper.game.unit-type :as unit-type]))

(def minimum-supply 8)

(defn under-minimum-supply? [supply]
  (<= (resources/supply->supply-available supply)
      minimum-supply))

(defn need-supply? [state]
  (let [supply (resources/get-supply (resources/get-state-resources state))
        capped? (resources/supply-capped? supply)
        under-minimum-supply? (under-minimum-supply? supply)]
    (and (not capped?) under-minimum-supply?)))

(defn can-afford? [state]
  (->> (unit-type/cost :supply-depot)
       (resources/can-afford? state)))

(defn building-supply? [state]
  (->> (jobs/get-unit-jobs state)
       (filter #(= (build/building %) :supply-depot))
       first))