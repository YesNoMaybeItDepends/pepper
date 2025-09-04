(ns user
  #_{:clj-kondo/ignore [:unused-namespace]}
  (:require
   [clojure.core.async :as a]
   [clojure.pprint :as pp]
   [clojure.string :as str]
   [portal.api :as portal]
   [pepper.dev :as dev]
   [pepper.utils.logging :as logging]
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

(defn load-edn
  "Load edn from an io/reader source (filename or io/resource)."
  [source]
  (try
    (with-open [r (clojure.java.io/reader source)]
      (edn/read {:readers {'mulog/flake com.brunobonacci.mulog.flakes/read-method}
                 :default (fn [t v] :no....)} (java.io.PushbackReader. r)))

    (catch java.io.IOException e
      (printf "Couldn't open '%s': %s\n" source (.getMessage e)))
    (catch RuntimeException e
      (printf "Error parsing edn file '%s': %s\n" source (.getMessage e)))))

(defn selected! []
  (first (portal/selected)))

(comment

  (set! *print-namespace-maps* false)

  @dev/store
  (tap> @dev/store)

  ;; start / stop

  (dev/start-pepper! {:async? true})
  dev/bot
  (future-done? dev/bot)
  (future-cancelled? dev/bot)
  (future-cancel dev/bot)

  (dev/start-pepper!)
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

  ;; logs

  (mu/log :tap :state (logging/format-state (dev/pepper!)))
  (tap> (logging/format-state (dev/pepper!)))

  (def x (load-edn (fs/file (last (fs/list-dir ".logs")))))
  (tap> (last x))

  (def x (:state (load-edn ".logs/1756728273095.log")))
  (tap> x)

  ;; misc 2

  (defn reset-jobs! [pepper-ref]
    (swap! pepper-ref update-in [:bot :unit-jobs] {}))

  ;; (defn tap-unit-id [unit-id]
  ;;   (let [pepper (pepper)]
  ;;     {:unit-job-record (user.portal/tap-unit-job unit-id)
  ;;      :unit (get-in pepper [:game :units-by-id unit-id])
  ;;      :unit-job (get-in pepper [:bot :unit-jobs unit-id])}))

  @user.portal/jobs-by-unit-id
  (tap> @user.portal/jobs-by-unit-id)

  (user.portal/tap-unit-job 171)
  ;; (tap-unit-id 171)

  #_())

(comment ;; quill

  (def my-sketch (drawing/sketch))
  (def my-sketch nil)
  my-sketch

  (use 'user.drawing :reload)
  (qa/with-applet user/my-sketch (q/start-loop))
  (qa/with-applet user/my-sketch (q/exit))
  (qa/with-applet user/my-sketch (q/random 10))
  (qa/with-applet user/my-sketch (q/no-loop))
  (qa/with-applet user/my-sketch (q/start-loop))

  #_())