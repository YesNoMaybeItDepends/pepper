(ns pepper.game.map
  (:refer-clojure :exclude [map])
  (:require [pepper.game.position :as position])
  (:import [bwapi Unit Pair]
           [bwem BWEM Neutral Base Area AreaId BWMap ChokePoint Altitude]))

(defn ->id [x]
  (condp instance? x
    bwem.Neutral (->id (Neutral/.getUnit x))
    bwem.Area (->id (Area/.getId x))
    bwem.AreaId (AreaId/.intValue x)
    bwem.Base (->id (Base/.getLocation x))
    bwem.ChokePoint (->id (ChokePoint/.getCenter x))
    bwapi.Unit (Unit/.getID x)
    bwapi.WalkPosition (position/->data x)
    bwapi.Position (position/->data x)
    bwapi.TilePosition (position/->data x)
    clojure.lang.PersistentArrayMap (:id x)
    clojure.lang.PersistentHashMap (:id x)
    clojure.lang.PersistentVector x
    nil))

(defn by-id [m x]
  (assoc m (->id x) x))

(defn reduce-by-id [coll]
  (reduce by-id {} coll))

(defn pair->tuple [pair]
  [(Pair/.getFirst pair)
   (Pair/.getSecond pair)])

(defn parse-base-on-start! [base]
  {:id (->id base)
   :area-id (->id (Area/.getId (Base/.getArea base)))
   :area-group (Area/.getGroupId (Base/.getArea base)) ;; 99% sure can be removed, always seems to be the same
   :mineral-fields (mapv ->id (Base/.getMinerals base))
   :blocking-mineral-fields (mapv ->id (Base/.getBlockingMinerals base))
   :geysers (mapv ->id (Base/.getGeysers base))
   :resource-depot-tile (position/->data (Base/.getLocation base))
   :center (position/->data (Base/.getCenter base))})

(defn parse-choke-point-on-start! [choke-point]
  {:id (->id choke-point) ;; (random-uuid)
   :areas (mapv ->id (pair->tuple (ChokePoint/.getAreas choke-point)))
   :blocking-neutral (->id (ChokePoint/.getBlockingNeutral choke-point))
   :center (position/->data (ChokePoint/.getCenter choke-point))
   :geometry (mapv position/->data (ChokePoint/.getGeometry choke-point))})

(defn parse-area-on-start! [area]
  {:id (->id area)
   :group-id (Area/.getGroupId area)
   :bases (mapv ->id (Area/.getBases area))
   :choke-point-ids (mapv ->id (Area/.getChokePoints area))
   :accessible-neighbor-ids (mapv ->id (Area/.getAccessibleNeighbors area))
   :top-left-tile (position/->data (Area/.getTop area))
   :bottom-right-tile (position/->data (Area/.getBottomRight area))
   :highest-altitude (Altitude/.intValue (Area/.getHighestAltitude area))
   :lowest-altitude (Altitude/.intValue (Area/.getHighestAltitude area))})

(defn parse-starting-bases-on-start! [map]
  (mapv ->id (BWMap/.getStartingLocations map)))

(defn parse-map-on-start! [bwem]
  (let [map (BWEM/.getMap bwem)]
    {:areas (reduce-by-id (mapv parse-area-on-start! (BWMap/.getAreas map)))
     :choke-points (reduce-by-id (mapv parse-choke-point-on-start! (BWMap/.getChokePoints map)))
     :bases (reduce-by-id (mapv parse-base-on-start! (BWMap/.getBases map)))
     :starting-bases (parse-starting-bases-on-start! map)}))

(defn starting-bases [map]
  (:starting-bases map))

(defn get-base-by-id [map base-id]
  (get-in map [:bases base-id]))

(defn get-base-area-id [base]
  (:area-id base))

(defn get-area-by-id [map area-id]
  (get-in map [:areas area-id]))

(defn get-area-id [area]
  (:id area))

(defn get-area-choke-point-ids [area]
  (:choke-point-ids area))

(defn get-choke-point-by-id [map choke-point-id]
  (get-in map [:choke-points choke-point-id]))

(defn get-choke-point-area-ids [choke-point]
  (:areas choke-point))

(defn get-area-accessible-neighbor-ids [area]
  (:accessible-neighbor-ids area))

(defn get-area-accessible-neighbors [area map]
  (->> (get-area-accessible-neighbor-ids area)
       (mapv #(get-area-by-id map %))))

(defn get-area-choke-points [area map]
  (->> (get-area-choke-point-ids area)
       (mapv #(get-choke-point-by-id map %))))

(defn get-base-area [base map]
  (get-area-by-id map (get-base-area-id base)))