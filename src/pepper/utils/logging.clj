(ns pepper.utils.logging
  (:require
   [com.brunobonacci.mulog :as mu]))

(defn init-logging! [file-name]
  (mu/start-publisher!
   {:type :multi
    :publishers [{:type :console}
                 {:type :simple-file :filename (str ".logs/" file-name ".log")}]}))

(defn format-state
  "dissoc :api
   
   stringify :action in unit-jobs"
  [state]
  (-> state
      (dissoc :api)
      (update-in [:bot :unit-jobs]
                 (fn [unit-jobs] (update-vals unit-jobs
                                              (fn [unit-job] (update unit-job :action str)))))))