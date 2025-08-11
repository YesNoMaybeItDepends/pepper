(ns pepper.game.auto-supply
  (:require [pepper.game.resources :as resources]
            [pepper.game.unit :as unit])
  (:import [bwapi UnitType]))

(def minimum-supply 8)

(defn under-minimum-supply? [supply]
  (<= (resources/supply->supply-available supply)
      minimum-supply))

(defn need-supply? [state]
  (let [supply (resources/get-supply state)
        capped? (resources/supply-capped? supply)
        under-minimum-supply? (under-minimum-supply? supply)]
    (and (not capped?) under-minimum-supply?)))

(defn can-afford? [state]
  (->> (resources/unit-type->cost UnitType/Terran_Supply_Depot)
       (resources/can-afford? state)))