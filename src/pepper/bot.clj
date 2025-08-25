(ns pepper.bot
  (:require
   [clojure.core.async :as a]
   [pepper.api.bwem :as bwem]
   [pepper.api.client :as client]
   [pepper.api.game :as api-game]
   [pepper.game.frame :as frame]
   [pepper.game.jobs :as jobs]
   [pepper.game.macro :as macro]
   [pepper.game.military :as military]
   [pepper.game.state :as state]
   [pepper.game :as gaem]
   [pepper.api :as api]
   [pepper.utils.config :as config]
   [taoensso.telemere :as tel])
  (:import
   [bwapi BWClient Game]))

(defn get-api [state]
  (:api state))

(defn get-game [state]
  (:game state))

(defn get-bot [state]
  (:bot state))

(defn on-start [state]
  (let [state (update state :api api/update-on-start)
        state (update state :game gaem/update-on-start
                      (gaem/parse-on-start (get-api state)))]
    (tap> state)
    state))

(defn on-frame [state]
  (-> state
      (state/update-state-with-frame-data (frame/parse-on-frame-data (get-api state)))
      (macro/process-macro)
      (military/maybe-find-enemy-starting-base)
      (jobs/process-state-jobs!)
      (state/render-state!)))

(defn on-end [state]
  state)

(defn init-state [api]
  {:api api
   :game {}
   :bot {}})

(defn init [api to-bot from-bot]
  (a/go-loop [state (init-state api)]
    (when-let [[id data :as event] (a/<! to-bot)]
      (let [state (case id
                    :on-start (on-start state)
                    :on-frame (on-frame state)
                    :on-end (on-end state)
                    :tap (do (tap> state) state)
                    state)]
        (a/>! from-bot :ack)
        (recur state)))))