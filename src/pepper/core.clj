(ns pepper.core
  (:require
   [clojure.core.async :as a]
   [pepper.api :as api]
   [pepper.bot :as bot]
   [pepper.game :as game]))

(defn api [state]
  (:api state))

(defn game [state]
  (:game state))

(defn bot [state]
  (:bot state))

(defn frame-last-run [state]
  (:frame-last-run state))

(defn set-last-frame-run [state frame]
  (assoc state :frame-last-run frame))

;;;; misc

(defn tapping [state]
  (tap> {:reason :tapping
         :state state})
  state)

(defn rendering [state]
  (game/render-game! (game state) (api state))
  (bot/render-bot! (bot state) (api state) (game state))
  state)

(defn throttling-by-game-frame [state state-fn]
  (let [to-skip 30
        now (.getFrameCount (api/game (api state)))
        last (or (frame-last-run state) 0)
        can-run? (> (- now last) to-skip)]
    (if can-run?
      (state-fn (set-last-frame-run state now))
      state)))

(defn skipping-if-paused [state state-fn]
  (if-not (.isPaused (api/game (api state)))
    (state-fn state)
    state))

;;;; on start

(defn on-start [state]
  (let [state (update state :api api/update-on-start)
        state (update state :game game/update-on-start
                      (game/parse-on-start (api state)))
        state (update state :bot bot/update-on-start
                      (bot/parse-on-start (api state)) (game state))]
    state))

;;;; on frame

(defn on-frame [state]
  (let [state (update state :game game/update-on-frame
                      (game/parse-on-frame (api state)))
        state (update state :bot bot/update-on-frame
                      (api state) (game state))]
    state))

;; consider...
;;
;; (defn parse-on-frame [state])
;; (defn update-on-frame [state])
;; (defn render-on-frame [state])
;; (defn on-frame [state]
;;   (-> state
;;       parse-on-frame
;;       update-on-frame ;; also moving side effects outside from update
;;       render-on-frame))

;;;; on end

(defn on-end [state]
  state)

;;;; handlers

(defn handle-error [e store]
  (tap> {:msg :error
         :state @store
         :error e}))

(defn handle-stop [store]
  (println "pepper stopping")
  (tap> {:msg :stopping
         :state @store}))

(defn handle-msg [state [id _ :as _] stop-ch]
  (case id
    :on-start (tapping (on-start state))
    :on-frame (-> state
                  (skipping-if-paused #(throttling-by-game-frame % on-frame))
                  rendering)
    :on-end (do (a/close! stop-ch)
                (tapping (on-end state)))
    :tap (tapping state)
    state))

(defn handle-res! [store msg stop-ch]
  (try
    (let [state (handle-msg @store msg stop-ch)]
      (swap! store merge state))
    (catch Exception e
      (handle-error e store))))

;;;; init

(defn stop? [[msg ch] stop-ch]
  (or (= ch stop-ch)
      (nil? msg)))

(defn init-state [api]
  {:api api
   :game {}
   :bot {}})

(defn init [api from-api to-api store stop-ch]
  (reset! store (init-state api))
  (a/go-loop []
    (when-let [[msg _ :as res] (a/alts! [stop-ch
                                         from-api]
                                        :priority true)]
      (if (stop? res stop-ch)
        (handle-stop store)
        (do (handle-res! store msg stop-ch)
            (a/>! to-api :done)
            (recur))))))