(ns pepper.strategy.spam-supply
  (:require
   [pepper.bwapi.game :as game]
   [pepper.jobs :as jobs]
   [pepper.jobs.build :as build]
   [pepper.supply :as supply]))

(defn need-supply?
  [player jobs]
  (and (supply/at-minimum-supply player)
       (supply/under-supply-limit player)
       (not (build/building-in-jobs? :supply-depot jobs))))

(defn maybe-build-supply [game jobs]
  (let [player (game/self game)]
    (when (need-supply? player jobs) (jobs/add! (build/job :supply-depot)))))