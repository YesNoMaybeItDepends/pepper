(ns pepper.bw.player
  (:require
   [pepper.api.player :as player]
   [pepper.api.game :as game]
   [pepper.api.unit :as unit]
   [pepper.api.unit-type :as unit-type]
   [clojure.java.data :as j]
   [pepper.bw.unit :as u]))

(defn player->data
  [player]
  {:id (player/get-id player)
   :name (player/get-name player)
   #_:units-by-id #_(u/units-by-id (player/get-units player))
   #_:force-id #_(player)
   #_:neutral? #_(player/is-ally? player)})

(defn players-by-id
  [game]
  (reduce
   (fn [players player] (assoc players (:id player) player))
   {} (map player->data (game/get-players game))))