(ns pepper.bot
  (:require
   [pepper.bot.macro :as macro]
   [pepper.bot.unit-jobs :as unit-jobs]
   [pepper.bot.military :as military]
   [pepper.bot.our :as our]))

(defn our [bot]
  (:our bot))

(defn macro [bot]
  (:macro bot))

(defn military [bot]
  (:military bot))

(defn unit-jobs [bot]
  (:unit-jobs bot))

;;;; on start

(defn parse-on-start [api]
  (our/parse-on-start api))

(defn update-on-start [{:as bot :or {}} data]
  (-> bot
      (update :our our/update-on-start data)
      (assoc :unit-jobs {})
      (assoc :macro {})
      (assoc :military {})))

;;;; on frame

(defn update-on-frame [bot api game]
  (-> bot
      (update :our our/update-on-frame)
      (update :macro macro/update-on-frame (our bot) (unit-jobs bot) game)
      (update :military military/update-on-frame (unit-jobs bot))
      (update :unit-jobs unit-jobs/update-on-frame api)))
