(ns pepper.game.upgrade
  (:require
   [clojure.set :as sql]
   [clojure.string :as str]
   [pepper.game.unit-type :as unit-type])
  (:import
   [bwapi UpgradeType]))

(defn- keywordize
  [enum]
  (-> enum
      str
      str/lower-case
      (str/replace #"_" "-")
      keyword))

(def ^:private by-keyword (zipmap (map keywordize (.getEnumConstants UpgradeType))
                                  (map identity (.getEnumConstants UpgradeType))))

(def ^:private by-object (sql/map-invert by-keyword))

(defn obj->kw [obj]
  (by-object obj))

(defn kw->obj [unit-type]
  (by-keyword unit-type))

(defn cost
  ([kw] (let [obj (kw->obj kw)]
          [(UpgradeType/.mineralPrice obj)
           (UpgradeType/.gasPrice obj)
           0]))
  ([kw n] (let [obj (kw->obj kw)]
            [(UpgradeType/.mineralPrice obj n)
             (UpgradeType/.gasPrice obj n)
             0])))

(defn uses [kw]
  (->> (UpgradeType/.whatUses (kw->obj kw))
       (mapv unit-type/object->keyword)))

(defn upgrades [kw]
  (->> (UpgradeType/.whatUpgrades (kw->obj kw))
       (mapv unit-type/object->keyword)))

(defn requires
  ([kw]
   (-> (kw->obj kw)
       UpgradeType/.whatsRequired
       unit-type/object->keyword))
  ([kw n]
   (-> (kw->obj kw)
       (UpgradeType/.whatsRequired n)
       unit-type/object->keyword)))