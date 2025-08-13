(ns pepper.game.unit-type
  (:refer-clojure :exclude [key])
  (:require
   [clojure.set :as sql]
   [clojure.string :as str]
   [pepper.game.resources :as resources])
  (:import
   [bwapi UnitType]))

(defn- keywordize-unit-type
  "Examples:
   
   Protoss_Dark_Templar -> :dark-templar
   
   Hero_Dark_Templar    -> :hero-dark-templar"
  [enum]
  (-> enum
      str
      str/lower-case
      (str/replace #"^(?!hero_)[^_]*_" "")
      (str/replace #"_" "-")
      keyword))

(def ^:private by-keyword (zipmap (map keywordize-unit-type (.getEnumConstants UnitType))
                                  (map identity (.getEnumConstants UnitType))))

(def ^:private by-object (sql/map-invert by-keyword))

(defn object->keyword [obj]
  (by-object obj))

(defn keyword->object [unit-type]
  (by-keyword unit-type))

(def mineral-field
  #{:mineral-field
    :mineral-cluster-type-1
    :mineral-cluster-type-2
    :mineral-field-type-2
    :mineral-field-type-3})

(def worker
  #{:scv :probe :drone})

(def town-hall
  #{:command-center :nexus :hatchery :lair :hive})

(defn cost [unit-type]
  (let [object (keyword->object unit-type)]
    (resources/quantity (UnitType/.mineralPrice object)
                        (UnitType/.gasPrice object)
                        (UnitType/.supplyRequired object))))