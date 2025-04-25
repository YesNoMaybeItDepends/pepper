(ns pepper.starcraft
  (:require
   [pepper.config :as config]
   [babashka.process :as p]))

(defn start-starcraft! []
  (p/process (get-in config/config [:starcraft :path])))

(defn stop-starcraft! []
  (p/sh "pskill starcraft")
  (p/sh "pskill chaoslauncher"))