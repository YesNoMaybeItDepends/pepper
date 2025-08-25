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
        state (update state :bot bot/update-on-frame)]
    state))

;;;; on end

(defn on-end [state]
  state)

;;;; init

(defn init-state [api]
  {:api api
   :game {}
   :bot {}})

(defn init [api from-api to-api]
  (a/go-loop [state (init-state api)]
    (when-let [[id data :as event] (a/<! from-api)]
      (let [state (case id
                    :on-start (tapping (on-start state))
                    :on-frame (on-frame state)
                    :on-end (tapping (on-end state))
                    :tap (tapping state)
                    state)]
        (a/>! to-api :ack)
        (recur state)))))

;;;; idea

;; (defn parse! [[state]]
;;   [state {}])

;; (defn plan! [[state input]]
;;   [state])

;; (defn execute! [[state]]
;;   [state])

;; (defn render! [[state]]
;;   state)

;; (defn _on-frame [state]
;;   (let [[state _] (-> [state]
;;                       parse!
;;                       plan!
;;                       execute!
;;                       render!)]
;;     state))
