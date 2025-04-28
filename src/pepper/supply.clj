(ns pepper.supply
  (:require [pepper.bwapi.unit :as unit]
            [pepper.bwapi.player :as player]))

(defn supply-capacity [player] (player/supply-total player))
(defn supply-used [player] (player/supply-used player))
(defn available-supply [player] (- (supply-capacity player) (supply-used player)))
(defn supply-limit [] 400)
(defn minimum-supply [] 2)

(defn at-minimum-supply [player] (<= (available-supply player) (minimum-supply)))
(defn under-supply-limit [player] (< (supply-capacity player) (supply-limit)))

(defn test-at-minimum-supply [used capacity] (<= (- capacity used) (minimum-supply)))
(defn test-under-supply-limit [capacity] (< capacity (supply-limit)))

(test-at-minimum-supply 8 10)
(test-under-supply-limit 400)

(defn id? [{id :id}]
  (some? id))

(defn unit? [{unit :unit}]
  (some? unit))

(defn building-completed? [game building]
  (unit/is-completed? (:unit building)))

(comment "idea for build job"

         (defn find-worker-job [] 1)
         (defn find-build-location-job [] 2)
         (defn move-to-build-location-job [])
         (defn build-something-job [])
         (defn listen-for-unit-create-job [])
         (defn listen-for-unit-complete-job [])
         (defn go-back-to-work-job [])

         (defn build-supply-job []
           (let [jobs (list find-worker-job
                            find-build-location-job
                            move-to-build-location-job
                            build-something-job
                            listen-for-unit-create-job
                            listen-for-unit-complete-job
                            go-back-to-work-job)]
             #_()))
         #_())