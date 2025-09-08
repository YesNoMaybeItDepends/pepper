(ns user
  #_{:clj-kondo/ignore [:unused-namespace]}
  (:require
   [clojure.core.async :as a]
   [clojure.pprint :as pp]
   [clojure.string :as str]
   [portal.api :as portal]
   [pepper.dev :as dev]
   [pepper.utils.logging :as logs]
   [user.portal :as user.portal]
   [clojure.edn :as edn]
   [com.brunobonacci.mulog :as mu]
   [quil.core :as q]
   [quil.middleware :as qm]
   [quil.applet :as qa]
   [user.drawing :as drawing]
   [snitch.core :refer [defn* defmethod* *fn *let]]
   [babashka.fs :as fs]))

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

(defn selected! []
  (first (portal/selected)))

(defn reset-jobs! [pepper-ref]
  (swap! pepper-ref update-in [:bot :unit-jobs] {}))

(comment

  (set! *print-namespace-maps* false)

  @dev/store
  (tap> @dev/store)

  ;; start / stop

  (dev/start-pepper-async!)
  dev/bot
  (future-done? dev/bot)
  (future-cancelled? dev/bot)
  (future-cancel dev/bot)

  (dev/start-pepper!)
  (dev/start-pepper-async!)
  (dev/stop-pepper!)

  ;; pepper

  (dev/pepper!)
  (tap> (dev/pepper!))

  ;; api

  (dev/api-client!)
  (dev/api-game!)
  (dev/api-bwem!)

  ;; pause / resume

  (do (dev/pause-game!)
      (tap> (dev/pepper!)))
  (dev/resume-game!)

  ;; 

  (reset-jobs! (dev/pepper-ref!))

  #_())

(comment ;; quill

  (def last-state (logs/state-from-last-log-file!))
  (def sketch (drawing/sketch last-state))
  (def sketch nil)

  sketch

  (use 'user.drawing :reload)
  (qa/with-applet user/sketch (q/start-loop))
  (qa/with-applet user/sketch (q/exit))
  (qa/with-applet user/sketch (q/random 10))
  (qa/with-applet user/sketch (q/no-loop))
  (qa/with-applet user/sketch (q/start-loop))

  #_())