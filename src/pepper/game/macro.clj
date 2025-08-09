(ns pepper.game.macro
  (:require
   [pepper.game.jobs :as jobs]
   [pepper.game.production :as production]
   [pepper.game.resources :as resources]
   [pepper.game.gathering :as gathering]
   [pepper.game.unit :as unit])
  (:import
   (bwapi Game Unit)))

(defn process-macro [state]
  (-> state
      resources/process-resources
      gathering/process-idle-workers
      production/process-idle-command-centers))
