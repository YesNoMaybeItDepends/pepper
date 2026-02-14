(ns pepper.main
  (:require
   [pepper.api :as api]
   [pepper.core :as pepper]
   [pepper.utils.config :as config]
   [pepper.utils.logging :as logging])
  (:gen-class))

(defn get-api-client-config [config]
  (:api config))

(defn get-bot-config [config]
  (:bot-config config))

(defn main [store]
  (logging/init-logging!)
  (let [config (config/read-config)
        api-config (get-api-client-config config)
        api (api/init api-config (pepper/event-handler store))
        bot-config (get-bot-config config)
        _ (pepper/init api store bot-config)]
    (logging/log {:event :starting-game})
    (api/start-game! api)
    (logging/log {:event :game-ended})))

(defn -main [& args]
  (main (atom {})))