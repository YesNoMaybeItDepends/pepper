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
   [snitch.core :refer [defn* defmethod* *fn *let]]))

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

  (declare quil-example)

  (defonce state-quil (ref nil))

  (dosync (ref-set state-quil (get-in x [:game :map])))

  (defn setup-quil []
    (q/frame-rate 1)
    (q/background 200)
    (dosync (ref-set state-quil {:game {:map {:choke-points {[300 300] {:center [300 300]
                                                                        :geometry [[300 290] [300 300] [300 310]]}}}}}))
    @state-quil)

  (defn update-quil [state]
    @state-quil)

  (defn draw-quil [state]
    (q/stroke 0)
    (q/stroke-weight 1)
    (doseq [choke-point (vals (:choke-points state))]
      (let [pos1 (first (:geometry choke-point))
            pos2 (:center choke-point)
            pos3 (last (:geometry choke-point))]
        (q/line pos1 pos2)
        (q/line pos2 pos3))))

  (q/defsketch quil-example
    :title "woah"
    :setup setup-quil
    :update update-quil
    :draw draw-quil
    :size [800 800]
    :middleware [qm/fun-mode])

  #_())