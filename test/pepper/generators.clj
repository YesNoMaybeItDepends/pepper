(ns pepper.generators
  (:require
   [clojure.math :refer [random]]
   [clojure.string :as str]
   [clojure.test.check.generators :as gen]
   [pepper.game.unit :as unit])
  (:import
   [bwapi UnitType]))

;;;; Etc



;;;; Unit types

(def unit-type (gen/elements (into [] (.getEnumConstants UnitType))))

(def unit-types-by-race (->> (group-by #(first (str/split (str %) #"_"))
                                       (.getEnumConstants UnitType))
                             (reduce (fn [acc [race units]]
                                       (case race
                                         "Zerg" (assoc acc :zerg units)
                                         "Terran" (assoc acc :terran units)
                                         "Protoss" (assoc acc :protoss units)
                                         acc))
                                     {:zerg []
                                      :terran []
                                      :protoss []})))

(def terran-unit-type (gen/elements (:terran unit-types-by-race)))
(def protoss-unit-type (gen/elements (:protoss unit-types-by-race)))
(def zerg-unit-type (gen/elements (:zerg unit-types-by-race)))
(def any-race-unit-type (gen/elements (flatten (vals unit-types-by-race))))

;;;; Units

(defn unit
  "opts --> {k [...]}
   :player-id 
   :type
   "
  ([] (unit {}))
  ([opts]
   (gen/hash-map
    :id gen/nat ;; We can just map a unique id after the fact lmao
    :player-id (if-some [id (:player-id opts)]
                 (gen/elements (flatten [id]))
                 gen/nat)
    :type (if-some [t (:type opts)]
            (gen/elements (flatten [t]))
            unit-type)
    :exists? (gen/return true)
    :idle? gen/boolean
    :frame-discovered (gen/return 0)
    :last-frame-updated gen/nat)))

;;;; Units by id

(defn units->units-by-id [units]
  (reduce
   (fn [m u]
     (assoc m (:id u) u))
   {}
   units))

(defn units->unique-units [units]
  (map-indexed
   (fn [idx unit]
     (assoc unit :id idx))
   units))

(defn units-by-id
  ([] (units-by-id {}))
  ([opts] (->> (gen/sample (unit opts))
               (units->unique-units)
               (units->units-by-id))))

;;;; Jobs

(defn mining-job
  ([] (mining-job {}))
  ([opts]
   (gen/hash-map
    :job (gen/return :mining)
    :unit-id gen/nat
    :mineral-field-id gen/nat
    :frame-started-gathering-minerals gen/nat
    :frame-issued-gather-command gen/nat
    :frame-last-executed gen/nat
    :completed? gen/boolean
    :action gen/keyword)))

(defn job
  ([] (job {}))
  ([opts] (gen/one-of [(mining-job opts) (gen/return nil)])))

;;;; Jobs by id (unit jobs)

(defn jobs->jobs-by-id [jobs]
  (reduce
   (fn [m j]
     (assoc m (:unit-id j) j))
   {}
   jobs))

(defn jobs->unique-jobs [jobs]
  (map-indexed
   (fn [idx job]
     (assoc job :unit-id idx))
   jobs))

(defn jobs-by-id []
  (->> (gen/sample (mining-job))
       (jobs->unique-jobs)
       (jobs->jobs-by-id)))

;;;; Resources

(def minerals gen/nat)
(def gas gen/nat)
(def supply (gen/fmap
             (fn [[x y]]
               [x (* x (+ y 1))])
             (gen/tuple gen/nat gen/nat)))
(def resources (gen/hash-map
                :minerals minerals
                :gas gas
                :supply supply))

;;;; State

(defn state-with-units
  ([state] (state-with-units state {}))
  ([state unit-opts] (assoc state :units-by-id (units-by-id unit-opts))))

(defn state-with-unit-jobs [state]
  (assoc state :unit-jobs (jobs-by-id)))

(defn state-with-some-nil-unit-jobs
  "percent-to-keep -> float, eg: 0.8"
  [state percent-to-keep]
  (update state :unit-jobs #(reduce-kv
                             (fn [m k v]
                               (if (> percent-to-keep (random))
                                 (assoc m k v)
                                 (assoc m k nil)))
                             {}
                             %)))

(defn state-with-resources [state]
  (assoc state :resources (rand-nth (gen/sample resources))))

(defn state-with-self-id [state id]
  (assoc state :self-id id))

(def state (gen/hash-map
            :resources resources
            #_:units-by-id #_units-by-id))

(comment ;; unit/get-idle-or-mining-worker
  (-> {}
      (state-with-units {:type [UnitType/Terran_SCV]
                         :player-id [1]})
      state-with-unit-jobs
      (state-with-some-nil-unit-jobs 0.8)
      state-with-resources
      (state-with-self-id 1)
      unit/get-idle-or-mining-worker))