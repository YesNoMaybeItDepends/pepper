(ns pepper.game.map
  (:require [pepper.game.position :as position])
  (:import [bwapi Unit Pair]
           [bwem BWEM Neutral Base Area AreaId BWMap ChokePoint Altitude]))

(defn ->id [x]
  (condp instance? x
    bwem.Neutral (->id (Neutral/.getUnit x))
    bwem.Area (->id (Area/.getId x))
    bwem.AreaId (AreaId/.intValue x)
    bwem.Base (->id (Base/.getCenter x))
    bwem.ChokePoint (->id (ChokePoint/.getCenter x))
    bwapi.Unit (Unit/.getID x)
    bwapi.WalkPosition (position/->data x)
    bwapi.Position (position/->data x)
    bwapi.TilePosition (position/->data x)
    clojure.lang.PersistentArrayMap (:id x)
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
  {:id (position/->data (Base/.getCenter base)) ;; (random-uuid)
   :area (Area/.getId (Base/.getArea base))
   :area-group (Area/.getGroupId (Base/.getArea base)) ;; nani?
   :mineral-fields (mapv ->id (Base/.getMinerals base))
   :blocking-mineral-fields (mapv ->id (Base/.getBlockingMinerals base))
   :geysers (mapv ->id (Base/.getGeysers base))
   :resource-depot-location (position/->data (Base/.getLocation base))
   :center (position/->data (Base/.getCenter base))})

(defn parse-choke-point-on-start! [choke-point]
  {:id (position/->data (ChokePoint/.getCenter choke-point)) ;; (random-uuid)
   :areas (mapv ->id (pair->tuple (ChokePoint/.getAreas choke-point)))
   :blocking-neutral (->id (ChokePoint/.getBlockingNeutral choke-point))
   :center (position/->data (ChokePoint/.getCenter choke-point))
   :geometry (mapv position/->data (ChokePoint/.getGeometry choke-point))})

(defn parse-area-on-start! [area]
  {:id (Area/.getId area)
   :group-id (Area/.getGroupId area)
   :bases (mapv ->id (Area/.getBases area))
   :choke-points (mapv ->id (Area/.getChokePoints area))
   :accesible-neighbors (mapv ->id (Area/.getAccessibleNeighbors area))
   :top-left (position/->data (Area/.getTop area))
   :bottom-right (position/->data (Area/.getBottomRight area))
   :highest-altitude (Altitude/.intValue (Area/.getHighestAltitude area))
   :lowest-altitude (Altitude/.intValue (Area/.getHighestAltitude area))})

(defn parse-map-on-start! [bwem]
  (let [map (BWEM/.getMap bwem)]
    {:areas (reduce-by-id (mapv parse-area-on-start! (BWMap/.getAreas map)))
     :choke-points (reduce-by-id (mapv parse-choke-point-on-start! (BWMap/.getChokePoints map)))
     :bases (reduce-by-id (mapv parse-base-on-start! (BWMap/.getBases map)))}))