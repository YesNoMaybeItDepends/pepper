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
   [user.portal :as portal]
   [pepper.game.unit :as unit]
   [pepper.bot.jobs.attack-move :as attack-move])
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
        data {:units (game/units game)
              :players (game/players game)
              :frame (game/frame game)
              :game-map (game/get-map game)
              :unit-jobs (vals unit-jobs)}

        [our messages] (our/update-on-frame
                        [our messages]
                        game)

        [macro messages] (macro/update-on-frame
                          [macro messages] game (vals unit-jobs))

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
  (position/->map (.getPosition unit-obj)))

(defn render-gather-job [job game _game]
  (let [{unit-id :unit-id
         mineral-field-id :target-id} job
        units-by-id (game/units-by-id _game)
        unit (get units-by-id unit-id)
        {xfrom :x yfrom :y} (unit/position unit)
        mineral (get units-by-id mineral-field-id)
        {xto :x yto :y} (unit/position mineral)]
    (Game/.drawLineMap game xfrom yfrom xto yto bwapi.Color/Blue)))

(defn render-build-job [job game]
  (when (and ((job/type? :build) job)
             (build/build-tile job))
    (let [worker (.getUnit game (job/unit-id job))
          worker-pos (.getPosition worker)
          unit-type (unit-type/keyword->object (build/building job))
          top-left (build/build-tile job)
          bottom-right (position/+ top-left {:x (UnitType/.tileWidth unit-type)
                                             :y (UnitType/.tileHeight unit-type)})
          pos1 (position/->position top-left)
          pos2 (position/->position bottom-right)]
      (Game/.drawLineMap game pos1 worker-pos bwapi.Color/Yellow)
      (Game/.drawBoxMap game pos1 pos2 bwapi.Color/Yellow))))

(defn render-attack-move-job [job game]
  (when (and ((job/type? :attack-move) job)
             (attack-move/target-position job))
    (let [unit (.getUnit game (job/unit-id job))
          unit-position (.getPosition unit)
          target-pos (position/->position (attack-move/target-position job))]
      #_(Game/.drawLineMap game unit-position target-pos bwapi.Color/Red))))

(defn filter-jobs-to-render [unit-jobs]
  (->> unit-jobs
       #_(filterv #(#{207 12 321 123} (:unit-id %)))
       #_(filterv #(#{:build} (job/type %)))))

(defn render-unit-jobs [unit-jobs game _game]
  (let [jobs-to-render (filter-jobs-to-render unit-jobs)
        unit-ids (mapv :unit-id jobs-to-render)]
    (portal/update-jobs jobs-to-render)
    (doseq [job jobs-to-render]
      (case (:job job)
        :gather (render-gather-job job game _game)
        :build (render-build-job job game)
        :attack-move (render-attack-move-job job game)
        :else))))

(defn render-bot! [bot api _game]
  (let [game (api/game api)]
    (render-unit-jobs (vals (unit-jobs bot)) game _game)))