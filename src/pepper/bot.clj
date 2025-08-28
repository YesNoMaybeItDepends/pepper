(ns pepper.bot
  (:require
   [clojure.pprint :as pp]
   [pepper.api :as api]
   [pepper.api.game :as api-game]
   [pepper.bot.macro :as macro]
   [pepper.bot.military :as military]
   [pepper.bot.our :as our]
   [pepper.bot.unit-jobs :as unit-jobs]))

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
  (let [{:keys [our macro military unit-jobs]} bot
        messages []
        [our messages] (our/update-on-frame
                        [our messages]
                        game)

        [macro messages] (macro/update-on-frame
                          [macro messages]
                          our unit-jobs game)

        [military messages] (military/update-on-frame
                             [military messages]
                             unit-jobs)

        [unit-jobs _] (unit-jobs/update-on-frame
                       [unit-jobs messages]
                       api)]
    (assoc bot
           :our our
           :macro macro
           :military military
           :unit-jobs unit-jobs)))

(defn render-bot! [bot api]
  (when false
    (api-game/draw-text-screen
     (api/get-game api) 0 15
     (with-out-str (pp/pprint (our bot))))))