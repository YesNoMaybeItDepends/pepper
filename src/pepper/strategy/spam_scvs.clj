(ns pepper.strategy.spam-scvs
  (:require
   [pepper.bw-api.game :as game]
   [pepper.bw-api.player :as player]
   [pepper.bw-api.unit :as unit]))

(defn maybe-train-workers [game]
  (let [player (game/self)
        units (player/get-units player)]

    (doseq [trainer (filter #(and (.isBuilding (:type %)) (seq (.buildsWhat (:type %)))) (map (fn [unit] {:unit unit :type (.getType unit)}) units))]

      (let [unit (first (.buildsWhat (:type trainer)))]
        (when (and (.canMake game unit (:unit trainer)) (< (unit/get-training-queue-count (:unit trainer)) 1))
          (.train (:unit trainer) unit))))))