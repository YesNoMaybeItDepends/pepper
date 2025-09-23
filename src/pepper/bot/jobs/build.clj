(ns pepper.bot.jobs.build
  (:require [pepper.bot.job :as job]
            [pepper.api :as api]
            [pepper.game.position :as position]
            [pepper.game.unit-type :as unit-type])
  (:import [bwapi Game Unit Player]))

;; problems
;; sometimes workers get conflicting build locations (probably)
;; so they never try building ever again, very sad

(defn building [job]
  (:building job))

(defn build-tile [job]
  (:build-tile job))

(defn frame-got-build-tile [job]
  (:frame-got-build-tile job))

(defn frame-issued-build-command [job]
  (:frame-issued-build-command job))

(defn frame-started-building [job]
  (:frame-started-building job))

(defn building-id [job]
  (:building-id job))

(defn valid-location? [location]
  ((complement #{bwapi.TilePosition/Invalid
                 bwapi.TilePosition/Unknown
                 bwapi.TilePosition/None})
   location))

(defn building-tiles
  "Given `target-tile` and `unit-type`, returns vector of tile-positions spanning the unit
     
     eg: [0 0] + [2 2]
   
     -> [[0 0] [0 1] [0 2]
         [1 0] [1 1] [1 2]
         [2 0] [2 1] [2 2]]"
  [target-tile unit-type]
  (let [{x :x y :y} target-tile
        {w :x h :y} (unit-type/tile-size unit-type)]
    (if (every? (every-pred (some-fn zero? pos?) int? some?) [x y w h])
      (vec (for [w (range w)
                 h (range h)]
             (position/_->tile-position {:x (+ x w)
                                         :y (+ y h)
                                         :scale :tile-position})))
      [])))

(declare get-build-tile!)
(declare go-build!)

(defn building-completed?! [api job]
  (let [game (api/game api)
        frame (Game/.getFrameCount game)
        worker (Game/.getUnit game (job/unit-id job))
        worker-constructing? (Unit/.isConstructing worker)
        building (Game/.getUnit game (building-id job))
        building-completed? (Unit/.isCompleted building)]
    (cond
      (or building-completed?
          (not worker-constructing?)) (job/set-completed job)
      :else job)))

(defn get-building-id! [api job]
  (let [game (api/game api)
        frame (Game/.getFrameCount game)
        worker (Game/.getUnit game (job/unit-id job))
        building? (Unit/.isConstructing worker)
        building (Unit/.getBuildUnit worker)]
    (cond
      (not building?) (job/set-completed job)
      (some? building) (-> job
                           (job/set-cost-paid frame)
                           (assoc :frame-started-building frame)
                           (assoc :building-id (Unit/.getID building))
                           (assoc :action #'building-completed?!))
      :else job)))

(defn go-build! [api job]
  (let [game (api/game api)
        frame (Game/.getFrameCount game)
        worker (Game/.getUnit game (job/unit-id job))
        tiles (mapv position/->tile-position (building-tiles (build-tile job) (building job)))
        building (unit-type/keyword->object (building job))
        tile (position/->tile-position (build-tile job))
        can-build? (Unit/.canBuild worker building tile)
        tiles-visible? (every? #(Game/.isVisible  game %) tiles)]
    (if tiles-visible?
      (if can-build?
        (do (Unit/.build worker building tile)
            (assoc job
                   :action #'get-building-id!
                   :frame-issued-build-command frame))
        (job/set-completed job))
      (if (not (Unit/.isMoving worker))
        (do (Unit/.move worker (bwapi.TilePosition/.toPosition tile))
            job)
        job))))

(defn get-build-location!
  "TODO: other than moving this somewhere else, maybe start looking from max and recur inwards to avoid funny building locations"
  [game building where min max]
  (loop [n min]
    (let [location (Game/.getBuildLocation game building where n)
          valid? (valid-location? location)]
      (if (or (and location
                   valid?)
              (<= max n))
        location
        (recur (inc n))))))

(defn get-build-tile! [api job]
  (let [game (api/game api)
        building (unit-type/keyword->object (building job))
        near-tile (if (:near-tile job)
                    (position/->tile-position (:near-tile job))
                    (Player/.getStartLocation (Game/.self game)))
        build-tile (if (= (:building job) :refinery)
                     (.getTilePosition (Game/.getUnit game (:geyser-id job)))
                     (get-build-location! game building near-tile 18 30))]
    (if build-tile
      (assoc job
             :build-tile (position/->map build-tile)
             :frame-got-build-tile (Game/.getFrameCount game)
             :action #'go-build!)
      job)))

(defn xform [[job api]]
  (case (:step job)
    :get-build-tile! #'get-build-tile!
    :go-build! #'go-build!
    :get-building-id! #'get-building-id!
    :building-completed?! #'building-completed?!))

(defn job
  ([unit-id unit-type] (job unit-id unit-type {}))
  ([unit-id unit-type {:keys [geyser-id near-tile] :as opts}]
   (merge
    {:job :build
     :building unit-type
     :cost (unit-type/cost unit-type)
     :action #'get-build-tile!
     :unit-id unit-id}
    (when geyser-id
      {:geyser-id geyser-id})
    (when near-tile
      {:near-tile near-tile}))))