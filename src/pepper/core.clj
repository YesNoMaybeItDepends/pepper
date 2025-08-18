(ns pepper.core
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

(defn maybe-log-state! [state config]
  (when (:log? config)
    (tel/log! (dissoc state :api/client :api/game :map))))

(defn maybe-pause-game! [game config]
  (let [paused? (Game/.isPaused game)
        frame (Game/.getFrameCount game)
        pause? (fn [paused? frame] (and (not paused?)
                                        (>= frame 50)))]
    (when (pause? paused? frame)
      (api-game/set-local-speed game :slowest)
      (Game/.pauseGame game))))

(defn on-start [{:api/keys [client] :as state}]
  (tel/event! :on-start)
  (let [game (BWClient/.getGame client)
        bwem (bwem/init! game)
        state (-> (assoc state :api/game game)
                  (assoc :api/bwem bwem)
                  (merge (state/init-state
                          (frame/parse-on-start-data game bwem))))]
    (tap> state)
    state))

(defn on-frame [{:api/keys [client game] :as state}]
  (maybe-log-state! state config/config)
  (-> state
      (state/update-state-with-frame-data (frame/parse-on-frame-data client game))
      (macro/process-macro)
      (military/maybe-scout)
      (jobs/process-state-jobs! game)
      (state/render-state!)))

(defn on-end [{:api/keys [client game] :as state}]
  (tap> state)
  (tel/event! :on-end)
  state)

(defn event-handler [[event data] state]
  (case event
    :on-start (on-start state)
    :on-frame (on-frame state)
    :on-end (on-end state)
    :tap (do (tap> state)
             state)
    :hello-world (do (println "Hello world!")
                     state)
    state))

(defn bot
  "TODO: bot could return [in out event-handler], 
   where event-handler is the fn passed to api/make-client to handle events"
  [state in out]
  (a/go-loop [state state]
    (let [[event-id data :as event] (a/<! in)
          state (#'event-handler event state)
          put? (a/>! out (or event-id
                             :done))]
      (recur state))))

(defn api [to-bot from-bot]
  (client/make-client (fn [event]
                        (a/>!! to-bot event)
                        (a/<!! from-bot))))

(defn main [store]
  (let [config (config/read-config)
        to-bot (a/chan)
        from-bot (a/chan)
        api (api to-bot from-bot)
        bot (bot {:api/client api} to-bot from-bot)]
    (swap! store assoc
           :api/client api
           :client-events-ch to-bot
           :bot-responses-ch from-bot)
    (client/run-starcraft! (client/chaos-launcher-path config)) ;; TODO: this should be moved to dev
    (client/start-game! api (:api config))
    (println "done")
    (client/kill-starcraft!))) ;; TODO: this should be moved to dev