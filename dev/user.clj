(ns user
  (:require
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

;;;; System

(defonce system (atom {}))

(stop-portal! system)
(init-portal! system)

(add-tap #'dev/store-api!)

(comment

  (set! *print-namespace-maps* false)

  @dev/store

  (dev/start-pepper! {:async? true})
  (dev/start-pepper!)
  (dev/stop-pepper!)


  (dev/tap-pepper!)


  (dev/get-client!)
  (dev/get-game!)
  (dev/get-bwem!)

  (dev/pause-game!)
  (dev/resume-game!)


  #_())
