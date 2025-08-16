(ns user
  (:require
   [clojure.core.async :as a]
   [portal.api :as portal]
   [pepper.dev :as dev]))

;;;; Portal

(defn init-portal! [store]
  (let [instance (portal/open {:theme :portal.colors/nord})]
    (add-tap #'portal/submit)
    (swap! store assoc :portal/instance instance)))

(defn stop-portal! [store]
  (let [instance (:portal/instance @store)]
    (portal/close instance)
    (portal/stop)
    (remove-tap #'portal/submit)
    (swap! store assoc :portal/instance nil)))

;;;; Pepper

(defn start-pepper! []
  (try
    (dev/main)
    (catch Exception e (println e))))

(defn stop-pepper! []
  (try
    (dev/reset)
    (catch Exception e (println e))))

(defn tap-pepper! []
  (let [state @dev/store
        in-chan (:api/in-chan state)
        out-chan (:api/out-chan state)
        event [:tap]]
    (a/>!! in-chan event)
    (a/<!! out-chan)))

(defn get-api-client! []
  (:api/client @dev/store))

(defn get-api-game! []
  (:api/game @dev/store))

(defn pause-game! []
  (bwapi.Game/.pauseGame (get-api-game!)))

(defn resume-game! []
  (bwapi.Game/.resumeGame (get-api-game!)))

;;;; System

(defonce system (atom {}))
(init-portal! system)

(comment

  (set! *print-namespace-maps* false)

  (start-pepper!)
  (stop-pepper!)

  (tap-pepper!)

  (get-api-client!)
  (get-api-game!)

  (pause-game!)
  (resume-game!)

  #_())
