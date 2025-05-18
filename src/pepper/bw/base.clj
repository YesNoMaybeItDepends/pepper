(ns pepper.bw.base
  (:require
   [pepper.api.player :as player]
   [pepper.api.game :as game]
   [pepper.api.unit :as unit]
   [pepper.api.unit-type :as unit-type]
   [clojure.java.data :as j]
   [pepper.bw.unit :as u])
  (:import (bwem BWEM)))

(defn log [x] (j/from-java-shallow x {}))
