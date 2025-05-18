(ns pepper.strategy.spam-scvs
  (:require
   [pepper.api.game :as game]
   [pepper.api.player :as player]
   [pepper.api.unit :as unit]))

(defn maybe-train-workers [game]
  (let [player (game/self game)
        units (player/get-units player)]

    (doseq [trainer (filter #(and (.isBuilding (:type %)) (seq (.buildsWhat (:type %)))) (map (fn [unit] {:unit unit :type (.getType unit)}) units))]

      (let [unit (first (.buildsWhat (:type trainer)))]
        (when (and (.canMake game unit (:unit trainer)) (< (unit/get-training-queue-count (:unit trainer)) 1))
          (.train (:unit trainer) unit))))))