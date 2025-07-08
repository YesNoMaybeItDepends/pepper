(ns pepper.core
  (:require
   [clojure.string :as str]
   [clojure.core.async :as a]
   [taoensso.telemere :as t]
   [pepper.api.client :as client]
   [pepper.api.game :as game]
   [pepper.utils.chaoslauncher :as chaoslauncher]))

(defn bot
  "TODO: bot could return [in out event-handler], 
   where event-handler is the fn passed to api/make-client to handle events"
  [state in out]
  (a/go-loop [state state]
    (let [event (a/<! in)
          state (case (:event event)
                  :on-start (let [state (update state :on-start (fnil inc 0))
                                  state (assoc state :game (.getGame (:api/client state)))]
                              state)
                  :on-frame (let [state (update state :on-frame (fnil inc 0))
                                  _ (game/draw-text-screen (:game state) 100 100 (str (game/get-frame-count (:game state))))]
                              state)
                  :on-end (let [state (update state :on-end (fnil inc 0))]
                            state)
                  state)
          put? (a/>! out true)]
      (println state)
      (recur state))))

;; with channels, events could be piped such as:
;; event
;; event -> state
;; event -> state -> intercept-before
;; event -> state -> intercept-before -> _process_
;; event -> state -> intercept-before -> _process_ -> intercept-after
;; event -> state -> intercept-before -> _process_ -> intercept-after -> out

(defn main [store]
  (let [whitelisted? (fn whitelisted?
                       [store]
                       (fn [event]
                         ((:api/event-whitelist @store) (:event event))))
        event-handler (fn event-handler
                        "TODO: pubsub instead of whitelist"
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
    (chaoslauncher/start!) ;; TODO: this should be moved to dev
    (client/start-game api {:async true
                            :debug-connection false
                            :log-verbosely false})
    (println "done")
    (chaoslauncher/stop!) ;; TODO: this should be moved to dev
    ))