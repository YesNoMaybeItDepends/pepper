(ns pepper.dev.chaos-launcher
  (:require
   [babashka.process :as p]))

(defn chaos-launcher-path [config]
  (get-in config [:starcraft :path]))

(defn run-starcraft!
  "Runs Starcraft by running Chaoslauncher.exe
   
   Requires the 'Run Starcraft on Startup' Chaoslauncher setting enabled."
  [chaos-launcher-path]
  (p/process chaos-launcher-path))

(defn kill-starcraft!
  "Kills both Starcraft and Chaoslauncher"
  []
  (p/sh "pskill starcraft")
  (p/sh "pskill chaoslauncher"))