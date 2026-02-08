(ns pepper.main
  (:require
   [clojure.core.async :as a]
   [com.brunobonacci.mulog :as mu]
   [pepper.api :as api]
   [pepper.core :as pepper]
   [pepper.utils.config :as config]
   [pepper.utils.logging :as logging])
  (:gen-class))

(defn get-api-client-config [config]
  (:api config))

(defn get-bot-config [config]
  (:bot-config config))

(defn get-api-before-start [state]
  (get-in state [:before-start] (fn [] #_(println "no before-start fn"))))

(defn get-api-after-end [state]
  (get-in state [:after-end] (fn [] #_(println "no after-end fn"))))

(defn main [store]
  (println "hello")
  (System/exit 0)
  (let [_ (logging/init-logging! (str (inst-ms (java.time.Instant/now))))
        [from-api to-api] [(a/chan) (a/chan)]
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
    (mu/log :starting-game)
    (api/start-game! api)
    (mu/log :closing-ch)
    (a/close! stop-ch)))

(defn -main [& args]
  (main (atom {})))