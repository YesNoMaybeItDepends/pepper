(ns pepper.generators.unit-gen
  (:require [clojure.string :as str]
            [pepper.game.unit :as unit]
            [clojure.test.check.generators :as gen])
  (:import [bwapi UnitType]))

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