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
  (portal/close)
  (portal/stop)
  (remove-tap #'portal/submit)
  (swap! store assoc :portal/instance nil))

;;;; Pepper

(defn start-pepper!
  ([] (start-pepper! {}))
  ([opts]
   (try
     (dev/main opts)
     (catch Exception e (println e)))))

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

(defn get-client! []
  (:api/client @dev/store))

(defn get-game! []
  (:api/game @dev/store))

(defn get-bwem! []
  (:api/bwem @dev/store))

(defn pause-game! []
  (let [game (get-game!)]
    (bwapi.Game/.setLocalSpeed game 167)
    (bwapi.Game/.pauseGame game)))

(defn resume-game! []
  (let [game (get-game!)]
    (bwapi.Game/.setLocalSpeed game 42)
    (bwapi.Game/.resumeGame game)))

(defn store-api! [x]
  (let [api-keys [:api/client :api/game :api/bwem]
        store @dev/store]
    (when (and (not-every? #(some? (% store)) api-keys)
               (every? #(some? (% x)) api-keys))
      (remove-tap #'store-api!)
      (swap! dev/store merge
             (reduce (fn [m k]
                       (assoc m k (k x)))
                     {}
                     api-keys)))))

;;;; System

(defonce system (atom {}))

(stop-portal! system)
(init-portal! system)

(add-tap #'store-api!)

(comment

  (set! *print-namespace-maps* false)

  @dev/store

  (start-pepper! {:async? true})
  (start-pepper!)
  (stop-pepper!)


  (tap-pepper!)


  (get-client!)
  (get-game!)
  (get-bwem!)

  (pause-game!)
  (resume-game!)


  #_())
