(ns pepper.game.state
  (:require
   [pepper.api.game :as api-game]
   [pepper.game.unit :as unit]))

(def state-keys #{:frame :units-by-id})

(defn init-state []
  {:frame nil
   :units-by-id {}})

(defn update-state [state frame-data]
  (-> state
      (assoc :frame (:frame frame-data))
      (update :units-by-id unit/update-units-by-id (:units frame-data))))

(defn render-state! [state]
  (let [{game :api/game
         frame :frame} state]
    (api-game/draw-text-screen game 100 100 (str "Frame: " frame)))
  state)