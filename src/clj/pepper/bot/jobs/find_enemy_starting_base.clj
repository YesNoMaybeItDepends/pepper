(ns pepper.bot.jobs.find-enemy-starting-base
  (:require
   [pepper.bot.job :as job]
   [pepper.api :as api]
   [pepper.game.position :as position]
   [pepper.game.unit-type :as unit-type])
  (:import
   [bwapi Game TilePosition Unit]))

(defn frame-issued-last-move-command [job]
  (:frame-issued-last-move-command job))

(defn positions [job]
  (:positions job))

(defn target-tile-position [job]
  (:target-tile-position job))

(defn target-position [job]
  (:target-position job))

(defn position-updates [job]
  (:position-updates job))

(defn yay!
  "TODO: we need to notify the state of scouting results. 
   no channels to pass messages, so may need to pass the state and update from here"
  [game job]
  job)

(defn are-we-there-yet?! [job api]
  (let [frame (Game/.getFrameCount (api/game api))
        scout (Game/.getUnit (api/game api) (job/unit-id job))
        scout-type (unit-type/object->keyword (Unit/.getType scout))
        scout-pos (position/->map (Unit/.getPosition scout))
        are-we-there-yet? (position/in-distance? scout-pos (target-position job)
                                                 (unchecked-divide-int (unit-type/sight-range scout-type) 2))]
    (if are-we-there-yet?
      (-> (update-in job [:position-updates (target-tile-position job)]
                     assoc
                     :frame-reached-target-position frame)
          (assoc :frame-reached-last-target-position frame
                 :target-position nil
                 :target-tile-position nil
                 :step :go-there!))
      job)))

(defn go-there! [job api]
  (let [frame (Game/.getFrameCount (api/game api))
        worker (Game/.getUnit (api/game api) (job/unit-id job))
        tile-pos (peek (positions job))
        ack (when tile-pos (Unit/.move
                            worker
                            (position/->position tile-pos)))]
    (if ack
      (-> (update-in job [:position-updates tile-pos]
                     assoc
                     :frame-issued-move-command frame
                     :target-position (position/_->position tile-pos)
                     :target-tile-position tile-pos)
          (assoc :target-position (position/_->position tile-pos)
                 :target-tile-position tile-pos
                 :frame-issued-last-move-command frame
                 :positions (pop (positions job))
                 :step :are-we-there-yet?!))
      job)))

(defn xform [[job api]]
  (case (:step job)
    :go-there! (#'go-there! job api)
    :are-we-there-yet?! (#'are-we-there-yet?! job api)
    :yay! (#'yay! job api)))

(def job
  {:job :find-enemy-starting-base
   :xform-id :find-enemy-starting-base
   :params {:positions "Positions to move to"
            :unit-id "Id of the unit that will be scouting"}
   :step :go-there!})