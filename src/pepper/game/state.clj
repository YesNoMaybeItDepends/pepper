(ns pepper.game.state
  (:require
   [pepper.api.game :as api-game]
   [pepper.game.unit :as unit]
   [pepper.game.player :as player]))

(def state-keys #{:frame :units-by-id})

(defn init-state [opts]
  (if (not= 0 (:frame opts))
    (throw (Exception. "The current frame should be 0"))
    {:frame (:frame opts)
     :players-by-id (player/update-players-by-id {} (:players opts))
     :self-id (:self-id opts)}))

(defn update-state [state frame-data]
  (-> state
      (assoc :frame (:frame frame-data))
      (update :units-by-id unit/update-units-by-id (:units frame-data))
      (update :player-by-id player/update-players-by-id (:players frame-data))))

(defn render-state! [state]
  (let [{game :api/game
         frame :frame} state]
    (api-game/draw-text-screen game 100 100 (str "Frame: " frame)))
  state)