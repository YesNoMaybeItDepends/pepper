(ns pepper.dev
  (:require
   [pepper.core :as pepper]
   [pepper.utils :as utils]
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
  (let [_ (utils/init-logging!)]
    (reset! store initial-store-state)
    (alter-var-root #'bot (constantly (future (pepper/main store))))))

(defn reset []
  (reset! bot initial-bot-state)
  (reset! store initial-store-state))

(comment

  (main)
  (reset)

  @store

  (let [state @store
        in-chan (:api/in-chan state)
        out-chan (:api/out-chan state)
        event {:event :tap}] ;; or {:event :hello-world}
    (a/>!! in-chan event)
    (a/<!! out-chan))

  (-> (:api/client @store)
      .getGame
      .resumeGame)

  (-> (:api/game @store)
      .pauseGame)

  (-> (:api/game @store)
      .resumeGame)

  #_())
