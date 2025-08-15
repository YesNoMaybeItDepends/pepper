(ns pepper.generators.player-gen
  (:require
   [clojure.test.check.generators :as gen]))

(def player (gen/hash-map :id gen/nat))