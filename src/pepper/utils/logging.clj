(ns pepper.utils.logging
  (:require
   [com.brunobonacci.mulog :as mu]))

(defn init-logging! [file-name]
  (mu/start-publisher!
   {:type :multi
    :publishers [{:type :console}
                 {:type :simple-file :filename (str ".logs/" file-name ".log")}]}))