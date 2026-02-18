(ns pepper.utils.logging
  (:require [clojure.pprint :as pp])
  (:import [java.time LocalTime]
           [java.time.format DateTimeFormatter]))

(defn HH-mm-ss! []
  (LocalTime/.format (LocalTime/now) (DateTimeFormatter/ofPattern "HH:mm:ss")))

(defn filter? [x]
  (some? (#{:job-cancelled} (:event x))))

(defn log? [x]
  (some? (#{:error :starting-game :on-start} (:event x))))

(defn log [x]
  (when (log? x)
    (pp/pprint {:log/HH-mm-ss (HH-mm-ss!)
                :log/data x})))

(defn filename []
  (str (inst-ms (java.time.Instant/now))))

(defn format-state
  "dissoc :api
   
   stringify :action in unit-jobs"
  [state]
  (-> state
      (dissoc :api)
      (assoc-in [:game :map :mini-tiles] :pls)
      (update-in [:bot :unit-jobs]
                 (fn [unit-jobs] (update-vals unit-jobs
                                              (fn [unit-job] (update unit-job :action str)))))))

(defn init-logging! []
  (log "init-logging! doesn't do anything right now"))