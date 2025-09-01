(ns user
  #_{:clj-kondo/ignore [:unused-namespace]}
  (:require
   [clojure.core.async :as a]
   [clojure.pprint :as pp]
   [clojure.string :as str]
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

(comment

  (set! *print-namespace-maps* false)

  @dev/store
  (tap> @dev/store)

  ;; start / stop

  (dev/start-pepper! {:async? true})
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
      (tap> (pepper)))

  (dev/resume-game!)

  ;; misc

  (defn reset-jobs! [pepper-ref]
    (swap! pepper-ref update-in [:bot :unit-jobs] {}))

  ;; (defn tap-unit-id [unit-id]
  ;;   (let [pepper (pepper)]
  ;;     {:unit-job-record (user.portal/tap-unit-job unit-id)
  ;;      :unit (get-in pepper [:game :units-by-id unit-id])
  ;;      :unit-job (get-in pepper [:bot :unit-jobs unit-id])}))

  ;; (defn metrics []
  ;;   (-> (pepper)
  ;;       :api
  ;;       api/metrics))

  @user.portal/jobs-by-unit-id
  (tap> @user.portal/jobs-by-unit-id)

  (user.portal/tap-unit-job 171)
  (tap-unit-id 171)

  ;; (metrics)

  #_())