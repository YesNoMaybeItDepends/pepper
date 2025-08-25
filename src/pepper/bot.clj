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
   [pepper.game :as game]
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

(defn set-api [state api]
  (assoc state :api api))

(defn set-game [state game]
  (assoc state :game game))

(defn set-bot [state bot]
  (assoc state :bot bot))

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

(defn on-start [state]
  (let [state (update state :api api/update-on-start)
        data (game/parse-on-start (get-api state))
        state (update state :game game/update-on-start data)]
    (tap> state)
    state))

(defn macro-update-on-frame [macro]
  #_(macro/process-macro)
  macro)

(defn military-update-on-frame [military]
  #_(military/maybe-find-enemy-starting-base)
  military)

(defn jobs-update-on-frame [jobs]
  #_(jobs/process-state-jobs!)
  jobs)

(defn render-on-frame [state]
  #_(state/render-state!)
  state)

(defn bot-update-on-frame [bot]
  (-> bot
      (update :macro macro-update-on-frame)
      (update :military military-update-on-frame)
      (update :jobs jobs-update-on-frame)))

(defn on-frame [state]
  (let [data (frame/parse-on-frame-data (get-api state))
        state (update state :game game/update-on-frame data)
        state (update state :bot bot-update-on-frame)]
    (-> state
        (update state :game game/update-on-frame data)
        (update state :bot bot-update-on-frame))
    #_(-> state
          #_(state/update-state-with-frame-data (frame/parse-on-frame-data (get-api state)))
          (macro/process-macro)
          (military/maybe-find-enemy-starting-base)
          (jobs/process-state-jobs!)
          (state/render-state!))))

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