(ns pepper.game.unit
  (:refer-clojure :exclude [type type?])
  (:require
   [pepper.game.player :as player]
   [pepper.game.position :as position]
   [pepper.game.unit-type :as unit-type])
  (:import
   (bwapi Game Player Unit WalkPosition)
   (bwem BWEM BWMap Area AreaId)))

(defn id [unit]
  (:id unit))

(defn player-id [unit]
  (:player-id unit))

(defn idle? [unit]
  (:idle? unit))

(defn exists? [unit]
  (:exists? unit))

(defn set-last-frame-updated [unit frame]
  (assoc unit :last-frame-updated frame))

(defn last-frame-updated [unit]
  (:last-frame-updated unit))

(defn type [unit]
  (:type unit))

(defn initial-type [unit]
  (:initial-type unit))

(defn type? [unit-types]
  (let [types (if (set? unit-types) unit-types
                  (into #{} (flatten [unit-types])))]
    (fn [unit]
      (types (type unit)))))

(defn initial-type? [unit-types]
  (let [types (if (set? unit-types) unit-types
                  (into #{} (flatten [unit-types])))]
    (fn [unit]
      (types (initial-type unit)))))

(defn resources [unit]
  (:resources unit))

(defn initial-resources [unit]
  (:initial-resources unit))

(defn frame-discovered [unit]
  (:frame-discovered unit))

(defn position [unit]
  (:position unit))

(defn tile [unit]
  (:tile unit))

(defn new-unit? [unit]
  (nil? (frame-discovered unit)))

(defn owned-by-player? [unit player]
  (= (player-id unit) (player/id player)))

(defn update-unit [unit new-unit]
  (merge unit new-unit))

(defn completed? [unit]
  (:completed? unit))

(defn visible? [unit]
  (:visible unit))

(defn set-dead [unit frame]
  (assoc unit :frame-destroyed frame))

(defn dead? [unit]
  (:frame-destroyed unit))

(defn datafy [obj kws kw->val]
  (reduce (fn [acc kw]
            (assoc acc kw ((kw kw->val) obj)))
          {}
          kws))

(def kw->val {:exists? Unit/.exists
              :id Unit/.getID
              :idle? Unit/.isIdle
              :player-id (comp Player/.getID Unit/.getPlayer)
              :type (comp unit-type/object->keyword Unit/.getType)
              :initial-type (comp unit-type/object->keyword Unit/.getInitialType)
              :position (comp position/->map Unit/.getPosition)
              :tile (comp position/->map Unit/.getTilePosition)
              ;; :areas (comp  Unit/.getPosition) ;; needs to use unit and bwem so gotta rethink this
              :completed? Unit/.isCompleted
              :visible? Unit/.isVisible
              :attack-frame? Unit/.isAttackFrame
              :attacking? Unit/.isAttacking
              :starting-attack? Unit/.isStartingAttack
              :under-attack? Unit/.isUnderAttack
              :resources Unit/.getResources
              :initial-position (comp position/->map Unit/.getInitialPosition)
              :initial-resources Unit/.getInitialResources
              :stimmed? Unit/.isStimmed
              :stim-timer Unit/.getStimTimer
              :burrowed? Unit/.isBurrowed})

(defn ->map
  ([unit-obj frame] (->map unit-obj frame (keys kw->val)))
  ([unit-obj frame keywords]
   (-> (datafy unit-obj keywords kw->val)
       (set-last-frame-updated frame))))

(defn parse-nearest-area! [^bwapi.TilePosition tile-position bwmap]
  (-> (BWMap/.getNearestArea bwmap tile-position)
      Area/.getId
      AreaId/.intValue))

(defn parse-area! [^bwapi.TilePosition tile-position bwmap]
  (when-some [area (BWMap/.getArea bwmap tile-position)]
    (AreaId/.intValue (Area/.getId area))))

(defn parse-unit!
  "DEPRECATED
   
   Reads a bwapi unit with a bwapi game"
  [game bwem]
  (fn [unit]
    (assoc (->map unit (Game/.getFrameCount game))
           :area (parse-area! (Unit/.getTilePosition unit) (BWEM/.getMap bwem)))))

(defn group-unit-by-keywords
  ([unit keywords] (group-unit-by-keywords {} unit keywords))
  ([acc unit keywords]
   (reduce
    (fn [acc kw]
      (update-in acc [kw (kw unit)] (fnil conj #{}) (id unit)))
    acc
    keywords)))

(defn group-units-by-keywords [units keywords]
  (reduce (fn [result unit]
            (group-unit-by-keywords result unit keywords))
          {}
          units))

(comment ;; poor man's test
  (let [input-keywords [:type :player]
        input-units [{:id 6
                      :type :scv
                      :player 1}
                     {:id 8
                      :type :marine
                      :player 2}]
        output {:type {:scv #{6}
                       :marine #{8}}
                :player {1 #{6}
                         2 #{8}}}]
    (assert (= (group-units-by-keywords input-units input-keywords)
               output)))
  #_())