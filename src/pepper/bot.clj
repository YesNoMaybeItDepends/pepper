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
   [pepper.utils.config :as config]
   [taoensso.telemere :as tel])
  (:import
   [bwapi BWClient Game]))

(defn on-start [{:keys [init-api] :as state}]
  (let [api (init-api)
        state (-> (assoc state :api api)
                  (merge (state/init-state
                          (frame/parse-on-start-data api))))]
    (tap> state)
    state))

(defn on-frame [{:keys [api] :as state}]
  (-> state
      (state/update-state-with-frame-data (frame/parse-on-frame-data api))
      (macro/process-macro)
      (military/maybe-find-enemy-starting-base)
      (jobs/process-state-jobs!)
      (state/render-state!)))

(defn on-end [{:keys [api] :as state}]
  state)

(defn api [to-bot from-bot]
  (let [client (client/make-client
                (fn [event]
                  (a/>!! to-bot event)
                  (a/<!! from-bot)))]
    [client (fn init []
              (let [game (BWClient/.getGame client)
                    bwem (bwem/init! game)]
                (fn get [x]
                  (case x
                    :client client
                    :game game
                    :bwem bwem))))]))

(defn bot [state to-bot from-bot]
  (a/go-loop [state state]
    (when-let [[event-id data :as event] (a/<! to-bot)]
      (let [state (case event-id
                    :on-start (on-start state)
                    :on-frame (on-frame state)
                    :on-end (on-end state)
                    :tap (do (tap> state) state)
                    state)]
        (a/>! from-bot :ack)
        (recur state)))))