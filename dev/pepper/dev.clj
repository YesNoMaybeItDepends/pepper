(ns pepper.dev
  (:require
   [pepper.core :as pepper]
   [pepper.utils.logging :as logging]))

(def initial-bot-state false)
(def initial-store-state {:api/event-whitelist #{:on-start :on-frame :on-end}
                          :api/client nil
                          :api/in-chan nil
                          :api/out-chan nil
                          :api/err-chan nil})

(defonce bot initial-bot-state)
(defonce store (atom initial-store-state))

(defn main [& opts]
  (let [_ (logging/init-logging! (str (inst-ms (java.time.Instant/now))))]
    (reset! store initial-store-state)
    (alter-var-root #'bot (constantly (future (pepper/main store))))))

(defn reset []
  (logging/stop-logging!)
  (reset! bot initial-bot-state)
  (reset! store initial-store-state))

