(ns pepper.dev
  (:require
   [clojure.core.async :as a]
   [pepper.api.game :as api-game]
   [pepper.dev.chaos-launcher :as chaos-launcher]
   [pepper.main :as pepper-main]
   [pepper.utils.config :as config]
   [pepper.utils.logging :as logging]
   [taoensso.telemere :as tel])
  (:import
   [bwapi Game]))

(def initial-bot-state false)
(def initial-store-state {:api nil
                          :ch-to-bot nil
                          :ch-from-bot nil
                          :error-ch nil})

(defonce bot initial-bot-state)
(defonce store (atom initial-store-state))

(defn main
  "this is here so i can run dev.main
   TODO: reconsider start-pepper!"
  ([] (main {}))
  ([{:keys [async?] :as opts}]
   (tap> opts)
   (let [_ (logging/init-logging! (str (inst-ms (java.time.Instant/now))))]
     (reset! store initial-store-state)
     (swap! store merge opts)
     (if async?
       (alter-var-root #'bot (constantly (future (pepper-main/main store))))
       (alter-var-root #'bot (constantly (pepper-main/main store)))))))

(defn reset []
  (logging/stop-logging!)
  (reset! bot initial-bot-state)
  (reset! store initial-store-state))

(defn maybe-log-state! [state]
  (tel/log! (dissoc state :api :game :map)))

(defn maybe-pause-game! [{:keys [api] :as state}]
  (let [game (api :game)
        paused? (Game/.isPaused game)
        frame (Game/.getFrameCount game)
        pause? (fn [paused? frame] (and (not paused?)
                                        (>= frame 50)))]
    (when (pause? paused? frame)
      (api-game/set-local-speed game :slowest)
      (Game/.pauseGame game))))


(defn api-config []
  {:before-start (fn [] (-> (config/read-config)
                            (chaos-launcher/chaos-launcher-path)
                            (chaos-launcher/run-starcraft!)))
   :on-start {:before nil
              :after nil}
   :on-frame {:before (fn [system]
                        (maybe-log-state! system)
                        (maybe-pause-game! system))
              :after nil}
   :on-end {:before nil
            :after nil}
   :after-end (fn [] (chaos-launcher/kill-starcraft!))})

(defn start-pepper!
  "Runs both Starcraft (through Chaoslauncher) and the bot
   TODO: maybe I shouldn't call this directly, and dev should only expose dev.main? "
  ([] (start-pepper! {}))
  ([opts]
   (try
     (main (merge (api-config) opts))
     (catch Exception e (println e)))))

(defn stop-pepper! []
  (try
    (reset)
    (catch Exception e (println e))))

(defn tap-pepper! []
  (let [state @store
        in-chan (:ch-to-bot state)
        out-chan (:ch-from-bot state)
        event [:tap]]
    (a/>!! in-chan event)
    (a/<!! out-chan)))

(defn get-client! []
  (:api @store))

(defn get-game! []
  (:game @store))

(defn get-bwem! []
  (:bwem @store))

(defn pause-game! []
  (let [game (get-game!)]
    (bwapi.Game/.setLocalSpeed game 167)
    (bwapi.Game/.pauseGame game)))

(defn resume-game! []
  (let [game (get-game!)]
    (bwapi.Game/.setLocalSpeed game 42)
    (bwapi.Game/.resumeGame game)))

(defn store-api! [x]
  (let [api-keys [:api :game :bwem]
        s @store]
    (when (and (not-every? #(some? (% s)) api-keys)
               (every? #(some? (% x)) api-keys))
      (remove-tap #'store-api!)
      (swap! store merge
             (reduce (fn [m k]
                       (assoc m k (k x)))
                     {}
                     api-keys)))))
