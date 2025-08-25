(ns pepper.bot
  (:require
   [pepper.bot.macro :as macro]
   [pepper.bot.jobs :as jobs]
   [pepper.bot.military :as military]
   [pepper.bot.our :as our]))

;;;; on start

(defn parse-on-start [api]
  (update {:our {}} :our (our/parse-on-start api)))

(defn update-on-start [{:as bot :or {}} api]
  (update bot :our our/update-on-start (our/parse-on-start api)))

;;;; on frame

(defn update-on-frame [bot]
  (-> bot
      (update :our our/update-on-frame)
      (update :macro macro/update-on-frame)
      (update :military military/update-on-frame)
      (update :jobs jobs/update-on-frame)))

(defn render-on-frame [state]
  #_(state/render-state!)
  state)
