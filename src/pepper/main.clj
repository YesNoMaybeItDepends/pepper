(ns pepper.main
  (:require
   [clojure.core.async :as a]
   [pepper.api.client :as api]
   [pepper.bot :as bot]
   [pepper.utils.config :as config]))

(defn main [store]
  (let [[to-bot from-bot] [(a/chan) (a/chan)]
        [client init-api] (bot/api to-bot from-bot)
        bot (bot/bot {:init-api init-api} to-bot from-bot)
        call-before-api-start (get-in @store [:before-start])
        call-after-api-end (get-in @store [:after-end])]
    (swap! store assoc
           :client-events-ch to-bot
           :bot-responses-ch from-bot)
    (call-before-api-start)
    (api/start-game! client (:api (config/read-config)))
    (call-after-api-end)))