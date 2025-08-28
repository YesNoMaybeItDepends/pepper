(ns user
  #_{:clj-kondo/ignore [:unused-namespace]}
  (:require
   [clojure.core.async :as a]
   [clojure.pprint :as pprint]
   [portal.api :as portal]
   [pepper.dev :as dev]
   [user.portal :as user.portal]))

;; consider these
;; pepper.core
;; pepper.core.api
;; pepper.core.game
;; pepper.core.bot

;;;; Portal

(defn init-portal! [store]
  (let [instance (portal/open {:launcher :vs-code})
        #_(portal/open {:theme :portal.colors/nord})]
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

(user.portal/init)

(defn pepper []
  @(:pepper-ref @dev/store))

(comment

  (set! *print-namespace-maps* false)

  @dev/store
  (tap> @dev/store)
  (tap> (pepper))
  (pepper)

  (dev/start-pepper! {:async? true})
  (dev/start-pepper!)
  (dev/stop-pepper!)


  (dev/tap-pepper!)


  (dev/get-client!)
  (dev/get-game!)
  (dev/get-bwem!)

  (dev/pause-game!)
  (do (dev/pause-game!)
      (tap> (pepper)))

  (dev/resume-game!)


  #_())