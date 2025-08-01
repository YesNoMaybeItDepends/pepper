(ns pepper.dev
  (:require
   [pepper.core :as pepper]
   [pepper.utils :as utils]
   [taoensso.telemere :as tel]
   [clojure.core.async :as a]))

(def initial-bot-state false)
(def initial-store-state {:api/event-whitelist #{:on-start :on-frame :on-end}
                          :api/client nil
                          :api/in-chan nil
                          :api/out-chan nil
                          :api/err-chan nil})

(defonce bot initial-bot-state)
(defonce store (atom initial-store-state))

(defn main [& opts]
  (let [_ (utils/init-logging! (str (inst-ms (java.time.Instant/now))))]
    (reset! store initial-store-state)
    (alter-var-root #'bot (constantly (future (pepper/main store))))))

(defn reset []
  (utils/stop-logging!)
  (reset! bot initial-bot-state)
  (reset! store initial-store-state))

