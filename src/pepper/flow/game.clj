(ns pepper.flow.game
  (:require
   [clojure.spec.alpha :as s]
   [pepper.bwapi.impl.game :as game]))

;; in
(s/def ::in-event any?)

;; out
(s/def ::out any?)

;;;; utils

(defn update-unhandled-events
  [state msg]
  (update-in state [:unhandled-events (java.time.Instant/now)]
             (fn [inst] (assoc inst (count inst) msg))))

;;;; flow 

(defn init-state
  [state]
  (assoc state
         :init-inst (java.time.Instant/now)))

(defn update-state
  [state message])

(defn update-on-start
  [state]
  (assoc state
         :start-inst (java.time.Instant/now)
         :had-game-on-start? (game/get-frame-count) ;; ?? haha
         ;;
         :frame (game/get-frame-count)
         :frame-inst (java.time.Instant/now)
         :paused (game/is-paused)))

(defn update-on-frame
  [state msg]
  (assoc state
         :frame (game/get-frame-count)
         :frame-inst (java.time.Instant/now)
         :paused (game/is-paused)))



(defn update-on-unhandled
  [state msg]
  (update-unhandled-events state msg))

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