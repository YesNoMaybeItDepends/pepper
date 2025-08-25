(ns pepper.main
  (:require
   [clojure.core.async :as a]
   [pepper.core :as pepper]
   [pepper.api :as api]
   [pepper.utils.config :as config]))

(defn get-api-client-config [config]
  (:api config))

(defn get-api-before-start [deref-store]
  (get-in deref-store [:before-start]))

(defn get-api-after-end [deref-store]
  (get-in deref-store [:after-end]))

(defn main [store]
  (let [[from-api to-api] [(a/chan) (a/chan)]
        api (api/init from-api to-api
                      (get-api-client-config (config/read-config))
                      (get-api-before-start @store)
                      (get-api-after-end @store))
        pepper (pepper/init api from-api to-api)
        _ (swap! store assoc
                 :pepper pepper
                 :ch-from-api from-api
                 :ch-to-api to-api)]
    (api/start-game! api)))