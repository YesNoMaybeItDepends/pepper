(ns pepper.utils
  (:require
   [clojure.string :as str]
   [taoensso.telemere :as t]))

;;;; logging

(defn init-logging! [file-name]
  (t/add-handler! :file-handler (t/handler:file {:path (str file-name ".log")
                                                 :output-fn (t/pr-signal-fn {:pr-fn :edn})})))

(defn stop-logging! []
  (t/stop-handlers!))

;;;; interop

(defn camel->kebab
  [string]
  (let [regex #"(?<![A-Z])[A-Z](?![A-Z])|(?<=[^A-Z:])[A-Z](?=[A-Z])|(?<=[A-Z])[A-Z](?![A-Z])(?=[a-z])"]
    (str/replace string regex #(str "-" %1))))