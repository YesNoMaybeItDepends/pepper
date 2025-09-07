(ns pepper.game.position
  (:refer-clojure :exclude [+ *])
  (:require
   [clojure.math :as math])
  (:import
   [bwapi
    Point
    Position
    TilePosition
    WalkPosition]))

(defn x [pos]
  (:x pos))

(defn y [pos]
  (:y pos))

(defn scale [pos]
  (:scale pos))

(def scale->n {:position      1
               :walk-position 8
               :tile-position 32})

(defn class->scale [obj]
  (get {Position     :position
        WalkPosition :walk-position
        TilePosition :tile-position} (class obj)))

(defn convert [from to]
  (fn [x]
    (math/floor-div (clojure.core/* x (scale->n from)) (scale->n to))))

(defn ->map
  ([position]
   (->map
    (Point/.getX position)
    (Point/.getY position)
    (class->scale position)))

  ([x y scale]
   {:x x
    :y y
    :scale scale}))

(defn ->position [{:keys [x y scale]}]
  (apply Position/new (mapv (convert scale :position) [x y])))

(defn ->walk-position [{:keys [x y scale]}]
  (apply WalkPosition/new (mapv (convert scale :walk-position) [x y])))

(defn ->tile-position [{:keys [x y scale]}]
  (apply TilePosition/new (mapv (convert scale :tile-position) [x y])))

(defn _->position [{:keys [x y scale] :as pos}]
  (merge pos (zipmap [:x :y] (mapv (convert scale :position) [x y]))))

(defn _->walk-position [{:keys [x y scale] :as pos}]
  (merge pos (zipmap [:x :y] (mapv (convert scale :walk-position) [x y]))))

(defn _->tile-position [{:keys [x y scale] :as pos}]
  (merge pos (zipmap [:x :y] (mapv (convert scale :tile-position) [x y]))))

(defn ->obj [{:keys [x y scale]}]
  (->> [x y]
       (apply (case scale
                :position Position/new
                :tile-position TilePosition/new
                :walk-position WalkPosition/new))))

(defn + [{x1 :x y1 :y :as pos1}
         {x2 :x y2 :y :as pos2}]
  (assoc pos1
         :x (clojure.core/+ x1 x2)
         :y (clojure.core/+ y1 y2)))
