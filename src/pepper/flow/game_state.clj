(ns pepper.flow.game-state
  (:require
   [clojure.spec.alpha :as s]
   [pepper.api.game :as game]
   [pepper.bw.player :refer [player->data]]
   [clojure.java.data :as j]))

;; in
(s/def ::in-event any?)

;; out
(s/def ::out any?)

;;;; utils

(defn timestamp []
  (java.time.Instant/now))

(defn update-unhandled-events
  [state msg]
  (update-in state [:unhandled-events (timestamp)]
             (fn [inst] (assoc inst (count inst) msg))))

;;;; flow 

(defn init-state
  [state]
  (assoc state
         :init-inst (timestamp)))

(defn update-state
  [state message])

(defn map-players-by-id
  []
  (into {} (map player->data (game/get-players))))

(defn update-on-start
  [state]
  (assoc state
         :start-inst (timestamp)
         :had-game-on-start? (game/get-frame-count) ;; ?? haha
         ;;
         :frame (game/get-frame-count)
         :frame-inst (timestamp)
         :paused (game/is-paused)
         :players-by-id (map-players-by-id)
         :self-id (game/self)))

(defn update-on-frame
  [state msg]
  (when (= (:frame state) 20)
    (game/pause-game))
  (assoc state
         :frame (game/get-frame-count)
         :frame-inst (timestamp)
         :paused (game/is-paused)))

(defn update-on-unhandled
  [state msg]
  (update-unhandled-events state msg))

(defn register-event [event]
  (assoc event
         :id (clojure.core/random-uuid)
         :frame (game/get-frame-count)
         :frame-inst (timestamp)))

(defn proc
  "Updates game state"

  ([] {:ins {::in-event "bwapi client event"}

       :outs {::out "output"}})

  ([args] (init-state args))

  ([state transition] state)

  ([state input msg]
   (case input
     ::in-event (case (:event msg)
                  :on-start [(update-on-start state) {::out [state]}]
                  :on-frame [(update-on-frame state msg) {::out [state]}]
                  [(update-on-unhandled state msg) {::out [state]}]))))