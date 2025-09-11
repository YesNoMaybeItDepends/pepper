(ns pepper.utils.logging
  (:require
   [babashka.fs :as fs]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [com.brunobonacci.mulog :as mu]
   [com.brunobonacci.mulog.flakes]
   [deed.base64 :as b64]
   [deed.core :as deed]))

(defn encode-to-file [x file]
  (deed/encode-to x file {:save-meta? false}))

(defn decode-from-file [file]
  (deed/decode-from file))

(defn encode-to-string [x]
  (b64/encode-to-base64-string x {:with-meta? false}))

(defn decode-from-string [base64-string]
  (b64/decode-from-base64-string base64-string))

(defn init-logging! [file-name]
  (mu/start-publisher!
   {:type :multi
    :publishers [#_{:type :console :pretty? true}
                 {:type :simple-file
                  :filename (str ".logs/" file-name ".log")
                  :transform (fn [events]
                               (map
                                (fn [event]
                                  (if (contains? event :state)
                                    (update event :state encode-to-string)
                                    event))
                                events))}]}))

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

(defn state [entry]
  (decode-from-string (:state entry)))

(defn load-last-log-file! []
  (load-edn (fs/file (last (fs/list-dir ".logs")))))

(defn state-from-last-log-file! []
  (-> (load-last-log-file!)
      (state)))