(ns pepper.bot
  (:require
   [clojure.pprint :as pp]
   [pepper.api :as api]
   [pepper.bot.job :as job]
   [pepper.bot.jobs.build :as build]
   [pepper.bot.macro :as macro]
   [pepper.bot.military :as military]
   [pepper.bot.our :as our]
   [pepper.bot.unit-jobs :as unit-jobs]
   [pepper.game :as game]
   [pepper.game.map :as map]
   [pepper.game.position :as position]
   [pepper.game.unit-type :as unit-type]
   [user.portal :as portal])
  (:import
   [bwapi Game UnitType]))

(defn our [bot]
  (:our bot))

(defn macro [bot]
  (:macro bot))

(defn military [bot]
  (:military bot))

(defn unit-jobs [bot]
  (:unit-jobs bot))

;;;; replace unit-jobs with these at some point

(defn unit-jobs-by-unit-id [bot]
  (:unit-jobs-by-id bot))

(defn unit-jobs-list [bot]
  (vals (unit-jobs-by-unit-id bot)))

;;;; on start

(defn parse-on-start [api]
  (our/parse-on-start api))

(defn update-on-start [{:as bot :or {}} data game]
  (let [bot (update bot :our our/update-on-start data)
        bot (assoc bot :unit-jobs {})
        bot (assoc bot :macro {})
        bot (assoc bot :military (military/update-on-start (game/our-player game) (map/starting-bases (game/get-map game))))]
    bot))

;;;; on frame

(defn update-on-frame [bot api game] ;; bro... this aint right either
  (let [messages []
        {:keys [our
                macro
                military
                unit-jobs]} bot
        data {:units (filterv :completed? (game/units game))
              :players (game/players game)
              :frame (game/frame game)
              :starting-bases (map/starting-bases (game/get-map game))
              :unit-jobs (vals unit-jobs)}

        [our messages] (our/update-on-frame
                        [our messages]
                        game)

        [macro messages] (macro/update-on-frame
                          [macro messages] data)

        [military messages] (military/update-on-frame
                             [military messages] data)

        [unit-jobs _] (unit-jobs/update-on-frame
                       [unit-jobs messages]
                       api)]
    (assoc bot
           :our our
           :macro macro
           :military military
           :unit-jobs unit-jobs)))

;;;; rendering and tapping

(defn edn->str [edn]
  (with-out-str (pp/pprint edn)))

(defn focus-camera-on [[x y] game]
  (.setScreenPosition game x y))

(defn unit-position [unit-obj]
  (position/->data (.getPosition unit-obj)))

;; (.drawLineMap game 0 0 x y bwapi.Color/White)

(defn render-build-job [job game]
  (when (and (job/type? job :build)
             (build/build-tile job))
    (let [worker (.getUnit game (job/unit-id job))
          worker-pos (.getPosition worker)
          unit-type (unit-type/keyword->object (build/building job))
          [left top] (build/build-tile job)
          top-left (.toPosition (position/->bwapi
                                 [left top]
                                 :tile-position))
          bottom-right (.toPosition (position/->bwapi
                                     [(+ left (UnitType/.tileWidth unit-type))
                                      (+ top (UnitType/.tileHeight unit-type))]
                                     :tile-position))]
      (Game/.drawLineMap game top-left worker-pos bwapi.Color/Yellow)
      (Game/.drawBoxMap game top-left bottom-right bwapi.Color/Yellow))))

(defn filter-jobs-to-render [unit-jobs]
  (->> unit-jobs
       #_(filterv #(#{207 12 321 123} (:unit-id %)))
       #_(filterv #(#{:build} (job/type %)))))

(defn render-unit-jobs [unit-jobs game]
  (let [jobs-to-render (filter-jobs-to-render unit-jobs)
        unit-ids (mapv :unit-id jobs-to-render)]
    (portal/update-jobs jobs-to-render)
    (doseq [job jobs-to-render]
      (render-build-job job game)))
  #_(api-game/draw-text-screen game 30 30 (edn->str unit-jobs)))

(defn render-bot! [bot api]
  (let [game (api/get-game api)]
    (render-unit-jobs (vals (unit-jobs bot)) game)))