(ns user
  #_{:clj-kondo/ignore [:unused-namespace]}
  (:require
   [clojure.core.async :as a]
   [clojure.pprint :as pprint]
   [portal.api :as portal]
   [pepper.dev :as dev]   ;; consider these
   [pepper.core :as core] ;; pepper.core
   [pepper.api :as api]     ;; pepper.core.api
   [pepper.game :as game]   ;; pepper.core.game
   [pepper.bot :as bot]     ;; pepper.core.bot
   [pepper.bot.unit-jobs :as bot.unit-jobs]
   [pepper.bot.macro :as bot.macro]
   [pepper.bot.macro.auto-supply :as macro.auto-supply]
   [pepper.game.unit :as game.unit]))

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