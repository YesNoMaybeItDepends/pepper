(ns pepper.generators
  (:require [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop])
  (:import [bwapi UnitType]))

;;;; Etc

(def gen-unit-type (gen/elements (into [] (.getEnumConstants UnitType))))

;;;; Resources

(def gen-minerals gen/nat)
(def gen-gas gen/nat)
(def gen-supply (gen/fmap
                 (fn [[x y]]
                   [x (* x (+ y 1))])
                 (gen/tuple gen/nat gen/nat)))
(def gen-resources (gen/hash-map
                    :minerals gen-minerals
                    :gas gen-gas
                    :supply gen-supply))

;;;; State

(def gen-state (gen/hash-map :resources gen-resources))
