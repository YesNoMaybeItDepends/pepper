(ns pepper.utils.logging
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [com.brunobonacci.mulog :as mu]
   [com.brunobonacci.mulog.flakes]))

(defn init-logging! [file-name]
  (mu/start-publisher!
   {:type :multi
    :publishers [#_{:type :console :pretty? true}
                 {:type :simple-file
                  :filename (str "bwapi-data/write/" file-name ".log")}]}))

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
(defn load-edn
  "Load edn from an io/reader source (filename or io/resource)."
  [source]
  (try
    (with-open [r (clojure.java.io/reader source)]
      (edn/read {:readers {'mulog/flake com.brunobonacci.mulog.flakes/read-method}
                 :default (fn [t v] ":no....")} (java.io.PushbackReader. r)))

    (catch java.io.IOException e
      (printf "Couldn't open '%s': %s\n" source (.getMessage e)))
    (catch RuntimeException e
      (printf "Error parsing edn file '%s': %s\n" source (.getMessage e)))))