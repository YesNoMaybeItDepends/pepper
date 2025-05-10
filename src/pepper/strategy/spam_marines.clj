(ns pepper.strategy.spam-marines
  (:require
   [pepper.bw-api.game :as game]
   [pepper.jobs :as jobs]
   [pepper.jobs.build :as build]
   [pepper.supply :as supply]))

(defn need-barracks? [player jobs]
  (let [supply (supply/supply-used player)
        barracks-count (build/building-count :barracks player)
        no-barracks-jobs (not (build/building-in-jobs? :barracks jobs))
        above-11-supply (>= supply (* 2 13))
        no-barracks (= barracks-count 0)]
    (and above-11-supply no-barracks no-barracks-jobs)))

(defn maybe-build-barracks [game jobs]
  (let [player (game/self)]
    (when (need-barracks? player jobs) (jobs/add! (build/job :barracks)))))

(defn run [game jobs]
  (maybe-build-barracks game jobs))