(ns pepper.game.unit
  (:refer-clojure :exclude [type type?])
  (:require
   [pepper.game.player :as player]
   [pepper.game.position :as position]
   [pepper.game.unit-type :as unit-type])
  (:import
   (bwapi Game Player Unit)))

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

(defn type?
  [unit unit-type]
  (contains? (if (set? unit-type) unit-type
                 (into #{} (flatten [unit-type])))
             (type unit)))

(defn frame-discovered [unit]
  (:frame-discovered unit))

(defn position [unit]
  (:position unit))

(defn new-unit? [unit]
  (nil? (frame-discovered unit)))

(defn owned-by-player? [unit player]
  (= (player-id unit) (player/id player)))

(defn update-unit [unit new-unit]
  (merge unit new-unit))

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
              :position (comp position/->data Unit/.getPosition)
              :tile (comp position/->data Unit/.getTilePosition)
              :completed? Unit/.isCompleted
              :visible? Unit/.isVisible})

(defn ->map
  ([unit-obj frame] (->map unit-obj frame (keys kw->val)))
  ([unit-obj frame keywords]
   (-> (datafy unit-obj keywords kw->val)
       (set-last-frame-updated frame))))

(defn parse-unit!
  "DEPRECATED
   
   Reads a bwapi unit with a bwapi game"
  [game]
  (fn [unit]
    (-> {}
        (assoc :id (Unit/.getID unit))
        (assoc :exists? (Unit/.exists unit))
        (assoc :last-frame-updated (Game/.getFrameCount game))
        (assoc :player-id (Player/.getID (Unit/.getPlayer unit)))
        (assoc :type (unit-type/object->keyword (Unit/.getType unit)))
        (assoc :idle? (Unit/.isIdle unit))
        (assoc :position (position/->data (Unit/.getPosition unit)))
        (assoc :tile (position/->data (Unit/.getTilePosition unit)))
        (assoc :completed? (Unit/.isCompleted unit)))))

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