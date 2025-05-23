(ns pepper.utils.chaoslauncher
  (:require
   [pepper.config :as config]
   [babashka.process :as p]))

(defn start! []
  (p/process (get-in (config/config) [:starcraft :path])))

(defn stop! []
  (p/sh "pskill starcraft")
  (p/sh "pskill chaoslauncher"))