(ns pepper.dev
  (:require
   [pepper.core :as pepper]
   [pepper.systems.logging :as logging]
   [pepper.repl :as repl]))

(def initial-bot-state false)
(def initial-store-state {:api/event-whitelist #{:on-start :on-frame :on-end}
                          :api/client nil
                          :api/in-chan nil
                          :api/out-chan nil
                          :api/err-chan nil})

(defonce bot initial-bot-state)
(defonce store (atom initial-store-state))

(defn main [& opts]
  ;; TODO: init portal ?
  (let [_ (logging/init-logging)]
    (reset! store initial-store-state)
    (alter-var-root #'bot (constantly (future (pepper/main store))))))

(defn reset []
  (reset! bot initial-bot-state)
  (reset! store initial-store-state))

(comment

  (main)
  (reset)

  #_())
