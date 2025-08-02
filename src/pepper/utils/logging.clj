(ns pepper.utils.logging
  (:require
   [taoensso.telemere :as tel]))

(defn init-logging! [file-name]
  (tel/add-handler! :file-handler (tel/handler:file {:path (str ".logs/" file-name ".log")
                                                     :output-fn (tel/pr-signal-fn {:pr-fn :edn})})))

(defn stop-logging! []
  (tel/stop-handlers!))