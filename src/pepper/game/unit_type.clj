(ns pepper.game.unit-type
  (:refer-clojure :exclude [key])
  (:require [clojure.string :as str]
            [clojure.set :as sql])
  (:import [bwapi UnitType]))

(def ^:private by-keyword (zipmap (->> (map str (.getEnumConstants UnitType))
                                       (map str/lower-case)
                                       (map #(str/replace % #"^[^_]*_" ""))
                                       (map #(str/replace % #"_" "-"))
                                       (map keyword))
                                  (map identity (.getEnumConstants UnitType))))

(def ^:private by-object (sql/map-invert by-keyword))

(defn object->keyword [obj]
  (by-object obj))

(defn keyword->object [unit-type]
  (by-keyword unit-type))

(comment ;; => (:mineral-cluster-type-1 :mineral-cluster-type-2 :mineral-field :mineral-field-type-2 :mineral-field-type-3)
  (->> (keys by-keyword)
       (filter #(str/includes? % "mineral"))))