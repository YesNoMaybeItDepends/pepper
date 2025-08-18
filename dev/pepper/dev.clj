(ns pepper.dev
  (:require
   [clojure.core.async :as a]
   [pepper.core :as pepper]
   [pepper.utils.logging :as logging]))

(def initial-bot-state false)
(def initial-store-state {:api/client nil
                          :client-events-ch nil
                          :bot-responses-ch nil
                          :error-ch nil})

(defonce bot initial-bot-state)
(defonce store (atom initial-store-state))

(defn main
  ([] (main {}))
  ([{:keys [async?]}]
   (let [_ (logging/init-logging! (str (inst-ms (java.time.Instant/now))))]
     (reset! store initial-store-state)
     (if async?
       (alter-var-root #'bot (constantly (future (pepper/main store))))
       (alter-var-root #'bot (constantly (pepper/main store)))))))

(defn reset []
  (logging/stop-logging!)
  (reset! bot initial-bot-state)
  (reset! store initial-store-state))

(defn start-pepper!
  ([] (start-pepper! {}))
  ([opts]
   (try
     (main opts)
     (catch Exception e (println e)))))

(defn stop-pepper! []
  (try
    (reset)
    (catch Exception e (println e))))

(defn tap-pepper! []
  (let [state @store
        in-chan (:client-events-ch state)
        out-chan (:bot-responses-ch state)
        event [:tap]]
    (a/>!! in-chan event)
    (a/<!! out-chan)))

(defn get-client! []
  (:api/client @store))

(defn get-game! []
  (:api/game @store))

(defn get-bwem! []
  (:api/bwem @store))

(defn pause-game! []
  (let [game (get-game!)]
    (bwapi.Game/.setLocalSpeed game 167)
    (bwapi.Game/.pauseGame game)))

(defn resume-game! []
  (let [game (get-game!)]
    (bwapi.Game/.setLocalSpeed game 42)
    (bwapi.Game/.resumeGame game)))

(defn store-api! [x]
  (let [api-keys [:api/client :api/game :api/bwem]
        s @store]
    (when (and (not-every? #(some? (% s)) api-keys)
               (every? #(some? (% x)) api-keys))
      (remove-tap #'store-api!)
      (swap! store merge
             (reduce (fn [m k]
                       (assoc m k (k x)))
                     {}
                     api-keys)))))