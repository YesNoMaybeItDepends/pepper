(ns pepper.core
  (:require
   [clojure.core.async :as a]
   [pepper.api.client :as client]
   [pepper.game.macro :as macro]
   [pepper.game.jobs :as jobs]
   [pepper.game.state :as state]
   [pepper.game.frame :as frame]
   [pepper.api.bwem :as bwem]
   [taoensso.telemere :as tel]
   [pepper.utils.config :as config])
  (:import
   [bwapi BWClient]))

(defn maybe-log-state! [state config]
  (when (:log? config)
    (tel/log! (dissoc state :api/client :api/game))))

(defn on-start [{:api/keys [client] :as state}]
  (let [game (BWClient/.getGame client)
        bwem (bwem/init! game)]
    (-> (assoc state :api/game game)
        (assoc :api/bwem bwem)
        (merge (state/init-state
                (frame/parse-on-start-data game bwem))))))

(defn on-frame [{:api/keys [client game] :as state}]
  (maybe-log-state! state config/config)
  (-> state
      (state/update-state-with-frame-data (frame/parse-on-frame-data client game))
      (macro/process-macro)
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

(defn main [store]
  (let [whitelisted? (fn whitelisted?
                       [store]
                       (fn [[event _]]
                         ((:api/event-whitelist @store) event)))
        ;; "TODO: pubsub instead of whitelist"
        event-handler (fn event-handler
                        [input-ch output-ch filter-pred]
                        (fn [event]
                          (tel/event! event)
                          (when (filter-pred event)
                            (let [put? (a/>!! input-ch event)
                                  take? (a/<!! output-ch)]
                              take?))))
        bot-in (a/chan)
        bot-out (a/chan)
        api-event-whitelist (whitelisted? store)
        api-handler (event-handler bot-in bot-out api-event-whitelist)
        api (client/make-client api-handler)
        _ (bot {:api/client api} bot-in bot-out)]
    (swap! store assoc
           :api/client api
           :api/event-whitelist #{:on-start :on-frame :on-end}
           :api/in-chan bot-in
           :api/out-chan bot-out)
    (client/run-starcraft! (client/chaos-launcher-path (config/read-config))) ;; TODO: this should be moved to dev
    (client/start-game! api {:async true
                             :unlimited-frame-zero true
                             :debug-connection false
                             :log-verbosely false})
    (println "done")
    (client/kill-starcraft!))) ;; TODO: this should be moved to dev