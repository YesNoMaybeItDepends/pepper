(ns pepper.game.unit
  (:refer-clojure :exclude [type type?])
  (:require [pepper.game.player :as player]
            [pepper.game.jobs :as jobs]
            [pepper.game.job :as job]
            [pepper.game.unit-type :as unit-type]
            [pepper.game.position :as position])
  (:import
   (bwapi Unit Game Player)))

(def unit-keys #{:id :player-id :type :exists? :idle? :frame-discovered :last-frame-updated})

(defn type [unit]
  (:type unit))

(defn id [unit]
  (:id unit))

(defn player-id [unit]
  (:player-id unit))

(defn get-units [state]
  (vals (:units-by-id state)))

(defn get-unit-by-id [state unit-id]
  (get-in state [:units-by-id unit-id]))

(defn update-unit-by-id [units-by-id unit]
  (update units-by-id (:id unit) merge unit))

(defn update-units-by-id [units-by-id units]
  (reduce update-unit-by-id units-by-id units))

(defn parse-unit!
  "Reads a bwapi unit with a bwapi game"
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

(defn type?
  [unit kind]
  (contains? (if (set? kind) kind
                 (into #{} (flatten [kind])))
             (type unit)))

(defn new-unit? [unit]
  (nil? (:frame-discovered unit)))

(defn ours? [state unit]
  (= (:player-id unit)
     (player/get-self-id state)))

(defn get-our-units [state]
  (->> (get-units state)
       (filter #(ours? state %))))

(defn idle? [unit]
  (:idle? unit))

(defn employed? [state unit]
  (some? (jobs/get-unit-job state (id unit))))

(defn unemployed? [state unit]
  ((complement employed?) state unit))

(defn get-workers [state]
  (->> (get-units state)
       (filter #(ours? state %))
       (filter #(type? % unit-type/worker))))

(defn get-idle-workers [state]
  (->> (get-workers state)
       (filter idle?)))

(defn with-job? [state job-type unit]
  (-> (jobs/get-unit-job state (id unit))
      (job/type? job-type)))

(defn group-workers-by-job [state]
  (->> (get-workers state)
       (group-by #(job/type (jobs/get-unit-job state (id %))))))

(defn get-mineral-fields [state]
  (->> (get-units state)
       (filter #(type? % unit-type/mineral-field))))

(defn get-idle-or-mining-worker [state]
  (let [workers-by-job (group-workers-by-job state)
        worker-id (cond (some? (get workers-by-job nil))
                        (:id (first (get workers-by-job nil)))

                        (some? (:mining workers-by-job))
                        (:id (first (:mining workers-by-job))))]
    (get-unit-by-id state worker-id)))