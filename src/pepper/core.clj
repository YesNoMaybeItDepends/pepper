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

;;;; misc

(defn tapping [x]
  (tap> x)
  x)

(defn rendering [state]
  (game/render-game! (game state) (api state))
  state)

;;;; on start

(defn on-start [state]
  (let [state (update state :api api/update-on-start)
        state (update state :game game/update-on-start
                      (game/parse-on-start (api state)))
        state (update state :bot bot/update-on-start
                      (bot/parse-on-start (api state)))]
    state))

;;;; on frame

(defn on-frame [state]
  (let [state (update state :game game/update-on-frame
                      (game/parse-on-frame (api state)))
        state (update state :bot bot/update-on-frame
                      (api state) (game state))]
    state))

;;;; on end

(defn on-end [state]
  state)

;;;; handlers

(defn handle-error [e state-ref stop-ch]
  (tap> e)
  (tap> {:reason :error
         :state @state-ref})
  (a/close! stop-ch))

(defn handle-stop [state-ref]
  (println "pepper stopping")
  (tap> {:reason :stopping
         :state @state-ref}))

(defn handle-msg [state [id _ :as msg] stop-ch]
  (case id
    :on-start (tapping (on-start state))
    :on-frame (-> state
                  on-frame
                  rendering)
    :on-end (do (a/close! stop-ch)
                (tapping (on-end state)))
    :tap (tapping state)
    state))

(defn handle-res! [state-ref msg stop-ch]
  (try
    (let [new-state (handle-msg @state-ref msg stop-ch)]
      (swap! state-ref merge new-state))
    (catch Exception e
      (handle-error e state-ref stop-ch))))

;;;; init

(defn stop? [[msg ch] stop-ch]
  (or (= ch stop-ch)
      (nil? msg)))

(defn init-state [api]
  {:api api
   :game {}
   :bot {}})

(defn init [api from-api state-ref stop-ch]
  (reset! state-ref (init-state api))
  (a/go-loop []
    (when-let [[msg _ :as res] (a/alts! [stop-ch
                                         from-api]
                                        :priority true)]
      (if (stop? res stop-ch)
        (handle-stop state-ref)
        (do (handle-res! state-ref msg stop-ch)
            (recur))))))