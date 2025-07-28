(ns pepper.core
  (:require
   [clojure.core.async :as a]
   [pepper.api.client :as client]
   [pepper.api.game :as api-game]
   [pepper.game.unit :as unit]
   [pepper.game.state :as game-state]
   [pepper.game.frame :as frame]
   [taoensso.telemere :as tel]
   [pepper.config :as config]
   [pepper.game.state :as state])
  (:import
   [bwapi BWClient Game Player]))

(defn on-start [{:api/keys [client] :as state} event]
  (let [game (BWClient/.getGame client)]
    (-> (assoc state :api/game game)
        (merge (state/init-state
                (frame/parse-on-start-data game))))))

(defn on-frame [{:api/keys [client game] :as state} event]
  (let [frame-data (-> (frame/parse-on-frame-data game)
                       (frame/with-event event))]
    (tel/log! frame-data)
    (-> (game-state/update-state state frame-data)
        (game-state/render-state!))))

(defn on-end [{:api/keys [client game] :as state} event]
  (tel/event! :on-end)
  state)

(defn event-handler [{event-name :event
                      :as event}
                     state]
  (case event-name
    :on-start (on-start state event)
    :on-frame (on-frame state event)
    :on-end (on-end state event)
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
    (let [event (a/<! in)
          state (#'event-handler event state)
          put? (a/>! out (if-let [event (:event event)]
                           event
                           :default))]
      (recur state))))

(defn main [store]
  (let [whitelisted? (fn whitelisted?
                       [store]
                       (fn [event]
                         ((:api/event-whitelist @store) (:event event))))
        ;; "TODO: pubsub instead of whitelist"
        event-handler (fn event-handler
                        [input-ch output-ch filter-pred]
                        (fn [event]
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
    (client/run-starcraft! (client/chaos-launcher-path (config/get-config))) ;; TODO: this should be moved to dev
    (client/start-game! api {:async true
                             :debug-connection false
                             :log-verbosely false})
    (println "done")
    (client/kill-starcraft!))) ;; TODO: this should be moved to dev