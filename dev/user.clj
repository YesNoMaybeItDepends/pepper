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
   ;; java 8 rip quil
   #_[quil.core :as q]
   #_[quil.middleware :as qm]
   #_[quil.applet :as qa]
   #_[user.drawing :as drawing]
   [snitch.core :refer [defn* defmethod* *fn *let]]
   [babashka.fs :as fs]))

;; consider these
;; pepper.core
;; pepper.core.api
;; pepper.core.game
;; pepper.core.bot

;;;; Portal

(defn init-portal! [store]
  (let [vs-code {:launcher :vs-code}
        default {:theme :portal.colors/nord}
        instance (portal/open default)]
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

(defn reset-job! [pepper-ref unit-id]
  (swap! pepper-ref update-in [:bot :unit-jobs] dissoc unit-id))

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

  ;; speed

  (dev/set-game-speed!)
  (dev/set-game-speed! 0)
  (dev/set-game-speed! 4)
  (dev/set-game-speed! 42)

  ;;

  (reset-jobs! (dev/pepper-ref!))
  (reset-job! (dev/pepper-ref!) 249)

  #_())

#_(comment ;; quill

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