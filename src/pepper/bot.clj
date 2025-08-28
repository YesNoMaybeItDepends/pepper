(ns pepper.bot
  (:require
   [clojure.pprint :as pp]
   [pepper.api :as api]
   [pepper.api.game :as api-game]
   [pepper.bot.job :as job]
   [pepper.bot.macro :as macro]
   [pepper.bot.military :as military]
   [pepper.bot.our :as our]
   [pepper.bot.unit-jobs :as unit-jobs]
   [pepper.game.position :as position]
   [user.portal :as portal]))

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

(defn update-on-start [{:as bot :or {}} data game]
  (let [bot (update bot :our our/update-on-start data)
        bot (update bot :unit-jobs {})
        bot (update bot :macro {})
        bot (assoc bot :military (military/update-on-start (our bot) game))]
    bot))

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
                             our unit-jobs game)

        [unit-jobs _] (unit-jobs/update-on-frame
                       [unit-jobs messages]
                       api)]
    (assoc bot
           :our our
           :macro macro
           :military military
           :unit-jobs unit-jobs)))

;;;; rendering and tapping effects

(defn focus-camera-on [[x y] api]
  (.setScreenPosition (api/get-game api) x y))

(defn unit-position [api-unit]
  (position/->data (.getPosition api-unit)))

(defn render-unit-ids [unit-ids api]
  (let [game (api/get-game api)]
    (doseq [unit-id unit-ids]
      (let [unit (.getUnit game unit-id)
            [x y] (unit-position unit)]
        (.drawLineMap game 0 0 x y bwapi.Color/White)
        (api-game/draw-text-map game x y (str unit-id))))))

(defn render-jobs [unit-jobs api]
  (api-game/draw-text-screen
   (api/get-game api) 30 30
   (with-out-str (pp/pprint unit-jobs))))

(defn jobs-to-render [unit-jobs-by-unit-id]
  (->> (vals unit-jobs-by-unit-id)
       (filterv #(job/type? % :build))))

(defn render-bot! [bot api]
  (let [jobs (jobs-to-render (unit-jobs bot))
        unit-ids (mapv :unit-id jobs)]
    (render-jobs jobs api)
    (portal/tap-jobs jobs)
    (render-unit-ids unit-ids api)))