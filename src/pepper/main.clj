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
        pepper-ref (atom {})
        stop-ch (a/chan)
        api (api/init from-api to-api
                      (get-api-client-config (config/read-config))
                      (get-api-before-start @store)
                      (get-api-after-end @store))
        pepper (pepper/init api from-api to-api pepper-ref stop-ch)
        _ (swap! store assoc
                 :pepper pepper
                 :pepper-ref pepper-ref
                 :ch-stop? stop-ch
                 :ch-from-api from-api
                 :ch-to-api to-api)]
    (api/start-game! api)
    (a/close! stop-ch)))