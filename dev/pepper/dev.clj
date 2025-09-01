(ns pepper.dev
  (:require
   [clojure.core.async :as a]
   [pepper.api.game :as api-game]
   [pepper.dev.chaos-launcher :as chaos-launcher]
   [pepper.main :as pepper-main]
   [pepper.core :as pepper]
   [pepper.api :as api]
   [pepper.utils.config :as config]
   [pepper.utils.logging :as logging])
  (:import
   [bwapi Game]))

(def initial-bot-state false)
(def initial-store-state {:api nil
                          :ch-from-api nil
                          :ch-to-api nil
                          :error-ch nil})

(defonce bot initial-bot-state)
(defonce store (atom initial-store-state))

(defn pepper-ref! []
  (get @store :pepper-ref))

(defn pepper! []
  @(pepper-ref!))

(defn api-client! []
  (api/client (pepper/api (pepper!))))

(defn api-game! []
  (api/game (pepper/api (pepper!))))

(defn api-bwem! []
  (api/bwem (pepper/api (pepper!))))

(defn api-config []
  {:before-start (fn [] (-> (config/read-config)
                            (chaos-launcher/chaos-launcher-path)
                            (chaos-launcher/run-starcraft!)))
   :after-end (fn [] (chaos-launcher/kill-starcraft!))})

(defn main
  "this is here so i can run dev.main
   TODO: reconsider start-pepper!"
  ([] (main {}))
  ([{:keys [async?] :as opts}]
   (let [_ (logging/init-logging! (str (inst-ms (java.time.Instant/now))))]
     (reset! store initial-store-state)
     (swap! store merge opts)
     (if async?
       (alter-var-root #'bot (constantly (future (pepper-main/main store))))
       (alter-var-root #'bot (constantly (pepper-main/main store)))))))

(defn reset []
  (reset! bot initial-bot-state)
  (reset! store initial-store-state))

(defn start-pepper!
  "Runs both Starcraft (through Chaoslauncher) and the bot
   TODO: maybe I shouldn't call this directly, and dev should only expose dev.main? "
  ([] (start-pepper! {}))
  ([opts]
   (main (merge (api-config) opts))
   ;;  (try

   ;;    (catch Exception e (println e)))
   ))

(defn stop-pepper! []
  (try
    (reset)
    (catch Exception e (println e))))

(defn pause-game! []
  (let [game (api-game!)]
    (bwapi.Game/.setLocalSpeed game 167)
    (bwapi.Game/.pauseGame game)))

(defn resume-game! []
  (let [game (api-game!)]
    (bwapi.Game/.setLocalSpeed game 42)
    (bwapi.Game/.resumeGame game)))