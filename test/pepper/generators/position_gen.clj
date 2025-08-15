(ns pepper.generators.position-gen
  (:require
   [clojure.test.check.generators :as gen]))

(def position (gen/tuple gen/nat gen/nat))