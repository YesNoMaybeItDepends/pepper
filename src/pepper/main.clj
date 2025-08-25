(ns pepper.main
  (:require
   [clojure.core.async :as a]
   [pepper.bot :as bot]
   [pepper.api :as api]
   [pepper.utils.config :as config]))

(defn get-api-client-config [config]
  (:api config))

(defn get-api-before-start [deref-store]
  (get-in deref-store [:before-start]))

(defn get-api-after-end [deref-store]
  (get-in deref-store [:after-end]))

(defn main [store]
  (let [[to-bot from-bot] [(a/chan) (a/chan)]
        api (api/init to-bot from-bot
                      (get-api-client-config (config/read-config))
                      (get-api-before-start @store)
                      (get-api-after-end @store))
        bot (bot/init api to-bot from-bot)
        _ (swap! store assoc
                 :pepper {:api api
                          :bot bot}
                 :ch-to-bot to-bot
                 :ch-from-bot from-bot)]
    (api/start-game! api)))