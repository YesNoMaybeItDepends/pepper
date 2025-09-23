(ns pepper.main
  (:require
   [clojure.core.async :as a]
   [pepper.core :as pepper]
   [pepper.api :as api]
   [pepper.utils.config :as config])
  (:gen-class))

(defn get-api-client-config [config]
  (:api config))

(defn get-bot-config [config]
  (:bot-config config))

(defn get-api-before-start [state]
  (get-in state [:before-start] (fn [] (println "no before-start fn"))))

(defn get-api-after-end [state]
  (get-in state [:after-end] (fn [] (println "no after-end fn"))))

(defn main [store]
  (let [[from-api to-api] [(a/chan) (a/chan)]
        config (config/read-config)
        pepper-ref (atom {})
        stop-ch (a/chan)
        api-config (get-api-client-config config)
        api (api/init from-api to-api
                      api-config
                      (get-api-before-start @store)
                      (get-api-after-end @store))
        bot-config (get-bot-config config)
        pepper (pepper/init api from-api to-api pepper-ref stop-ch bot-config)
        _ (swap! store assoc
                 :pepper pepper
                 :pepper-ref pepper-ref
                 :ch-stop? stop-ch
                 :ch-from-api from-api
                 :ch-to-api to-api)]
    (api/start-game! api)
    (a/close! stop-ch)))

(defn -main [& args]
  (main (atom {})))