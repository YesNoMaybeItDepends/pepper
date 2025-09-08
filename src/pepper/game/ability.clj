(ns pepper.game.ability
  (:require
   [clojure.set :as sql]
   [clojure.string :as str]
   [pepper.game.order :as order]
   [pepper.game.unit-type :as unit-type])
  (:import
   [bwapi TechType]))

(defn- keywordize
  [enum]
  (-> enum
      str
      str/lower-case
      (str/replace #"_" "-")
      keyword))

(def ^:private by-keyword (zipmap (map keywordize (.getEnumConstants TechType))
                                  (map identity (.getEnumConstants TechType))))

(def ^:private by-object (sql/map-invert by-keyword))

(defn obj->kw [obj]
  (by-object obj))

(defn kw->obj [unit-type]
  (by-keyword unit-type))

(defn cost [kw]
  (let [obj (kw->obj kw)]
    [(TechType/.mineralPrice obj)
     (TechType/.gasPrice obj)
     0]))

(defn researches [kw]
  (let [obj (kw->obj kw)]
    (unit-type/object->keyword (TechType/.whatResearches obj))))

(defn uses [kw]
  (mapv unit-type/object->keyword
        (-> kw kw->obj TechType/.whatUses)))

(defn targets-unit? [kw]
  (-> kw kw->obj TechType/.targetsUnit))

(defn targets-position? [kw]
  (-> kw kw->obj TechType/.targetsPosition))

(defn requires [kw]
  (-> kw kw->obj TechType/.requiredUnit unit-type/object->keyword))

(defn order [kw]
  (-> kw kw->obj TechType/.getOrder order/obj->kw))

(defn energy-cost [kw]
  (-> kw kw->obj TechType/.energyCost))
(keys by-keyword)
;; getWeapon
;; getRace
;; researchTime