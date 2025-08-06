(ns pepper.generators
  (:require [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop])
  (:import [bwapi UnitType]))

;;;; Etc

(def unit-type (gen/elements (into [] (.getEnumConstants UnitType))))

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

(def state (gen/hash-map :resources resources))
