(ns pepper.game.ability
  (:require
   [clojure.set :as sql]
   [clojure.string :as str]
   [pepper.game.order :as order]
   [pepper.game.race :as race]
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

(def by-keyword (zipmap (map keywordize (.getEnumConstants TechType))
                        (map identity (.getEnumConstants TechType))))

(def by-object (sql/map-invert by-keyword))

(defn obj->kw [obj]
  (by-object obj))

(defn kw->obj [unit-type]
  (by-keyword unit-type))

(defn race [kw]
  (-> kw kw->obj TechType/.getRace race/object->keyword))

(def by-race (group-by race (keys by-keyword)))

(defn cost [kw]
  (let [obj (kw->obj kw)]
    [(TechType/.mineralPrice obj)
     (TechType/.gasPrice obj)
     0]))

(defn researches [kw]
  (-> (kw->obj kw)
      TechType/.whatResearches
      unit-type/object->keyword))

(defn uses [kw]
  (->> (TechType/.whatUses (kw->obj kw))
       (mapv unit-type/object->keyword)))

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

;; getWeapon
;; researchTime