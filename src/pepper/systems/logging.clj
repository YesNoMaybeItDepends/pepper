(ns pepper.systems.logging
  (:require [taoensso.telemere :as t]))

(defn init-logging []
  (t/add-handler! :file-handler (t/handler:file {:path ".log"
                                                 :output-fn (t/pr-signal-fn {:pr-fn :edn})})))