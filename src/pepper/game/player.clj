(ns pepper.game.player
  (:refer-clojure :exclude [name force type])
  (:require
   [pepper.game.color :as color]
   [pepper.game.position :as position]
   [pepper.game.race :as race])
  (:import
   [bwapi Force Game Player]))

(defn id [player]
  (:id player))

(defn force [player]
  (:force player))

(defn minerals [player]
  (:minerals player))

(defn gas [player]
  (:gas player))

(defn supply-total [player]
  (:supply-total player))

(defn supply-used [player]
  (:supply-used player))

(defn supply [player]
  [(supply-used player) (supply-total player)])

(defn supply-available [player]
  (let [total (supply-total player)
        used (supply-used player)]
    (- total used)))

(defn resources-available [player]
  [(minerals player) (gas player) (supply-available player)])

(defn starting-base [player]
  (:starting-base player))

;;;; players

(defn update-player [player new-player]
  (merge player new-player))

(defn our-player [players]
  (first (filter :self? players)))

(defn neutral-player [players]
  (first (filter :neutral? players)))

(defn enemy-player [players]
  (first (filter :enemy? players)))

;;;; parsing

(defn parse-player!
  "player-obj -> player"
  [game-obj]
  (fn [player-obj]
    (let [id (Player/.getID player-obj)
          self-id (Player/.getID (Game/.self game-obj))
          enemy-id (Player/.getID (Game/.enemy game-obj))]
      (-> {}
          (assoc :id id)
          (assoc :name (Player/.getName player-obj))
          (assoc :race (race/object->keyword (Player/.getRace player-obj)))
          (assoc :force (Force/.getID (Player/.getForce player-obj)))
          (assoc :color (color/object->keyword (Player/.getColor player-obj)))
          (assoc :supply-total (Player/.supplyTotal player-obj))
          (assoc :supply-used (Player/.supplyUsed player-obj))
          (assoc :starting-base (position/->map (Player/.getStartLocation player-obj)))
          (assoc :neutral? (Player/.isNeutral player-obj))
          (assoc :self? (= id self-id))
          (assoc :enemy? (= id enemy-id))
          (merge {:minerals (Player/.minerals player-obj)
                  :minerals-spent (Player/.spentMinerals player-obj)
                  :minerals-gathered (Player/.gatheredMinerals player-obj)
                  :minerals-refunded (Player/.refundedMinerals player-obj)
                  :minerals-repaired (Player/.repairedMinerals player-obj)}
                 {:gas (Player/.gas player-obj)
                  :gas-spent (Player/.spentGas player-obj)
                  :gas-gathered (Player/.gatheredGas player-obj)
                  :gas-refunded (Player/.refundedGas player-obj)
                  :gas-repaired (Player/.repairedGas player-obj)})))))