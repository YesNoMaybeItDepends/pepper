(ns pepper.core
  (:require
   [clojure.core.async :as a]
   [pepper.api.client :as client]
   [pepper.api.game :as game]
   [pepper.utils.chaoslauncher :as chaoslauncher]
   [taoensso.telemere :as t])
  (:import
   [bwapi Game BWClient]))

(defn on-start [{:api/keys [client] :as state} event]
  (let [game (BWClient/.getGame client)]
    (assoc state
           :api/game game
           :api/metrics (BWClient/.getPerformanceMetrics client)
           :game/players (Game/.getPlayers game)
           :game/self (Game/.self game)
           :game/units (Game/.getAllUnits game))))

(defn on-frame [{:api/keys [client game] :as state} event]
  (let [frames-behind (BWClient/.framesBehind client)
        frame (Game/.getFrameCount game)]
    (assoc state
           :game/frame frame
           :api/frames-behind frames-behind)))

(defn render-state [state event]
  (let [{game :api/game
         frame :game/frame
         frames-behind :api/frames-behind} state]
    (game/draw-text-screen game 100 100 (str "Frame: " frame))
    (game/draw-text-screen game 100 110 (str "Frames behind:" frames-behind)))
  state)

(defn on-end [{:api/keys [client game] :as state} event]
  (t/event! :on-end)
  state)

(defn event-handler [{event-name :event
                      :as event}
                     state]
  (case event-name
    :on-start (on-start state event)
    :on-frame (-> state
                  (on-frame event)
                  (render-state event))
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
    (chaoslauncher/start!) ;; TODO: this should be moved to dev
    (client/start-game api {:async true
                            :debug-connection false
                            :log-verbosely false})
    (println "done")
    (chaoslauncher/stop!))) ;; TODO: this should be moved to dev
    