(ns pepper.game.state
  (:require
   [pepper.api.game :as api-game]
   [pepper.game.unit :as unit]
   [pepper.game.player :as player]))

(defn set-frame [state frame]
  (assoc state :frame frame))

(defn get-frame [state]
  (:frame state))

(defn init-state [opts]
  (if (not= 0 (:frame opts))
    (throw (Exception. "The current frame should be 0"))
    (-> {}
        (set-frame (:frame opts))
        (player/update-players (:players opts))
        (player/set-self-id (:self opts))
        (assoc :map (:map opts)))))

(defn update-state-with-frame-data [state frame-data]
  (-> state
      (merge (select-keys frame-data [:frame :frames-behind
                                      :latency-frames :latency-time
                                      :latency-remaining-frames
                                      :latency-remaining-time]))
      (update :units-by-id unit/update-units-by-id (:units frame-data))
      (update :players-by-id player/update-players-by-id (:players frame-data))))

(defn render-state! [state]
  (let [{api :api
         frame :frame} state]
    (api-game/draw-text-screen (api :game) 100 100 (str "Frame: " frame)))
  state)