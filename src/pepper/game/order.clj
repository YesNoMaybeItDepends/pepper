(ns pepper.game.order
  (:require [clojure.string :as str]
            [clojure.set :as sql])
  (:import [bwapi Order]))

(defn- keywordize
  "TODO: kebabify prepending - to upper case letters except the first one
   examples:
   
   DroneLiftOff
   DroneStartBuild
   EnableDoodad
   EnterNydusCanal
   EnterTransport
   Fatal
   FireYamatoGun
   Follow
   Guard
   GuardianAspect
   GuardPost
   Hallucination2
   HarassMove"
  [enum]
  (-> enum
      str
      str/lower-case
      (str/replace #"_" "-")
      keyword))

(def ^:private by-keyword (zipmap (map keywordize (.getEnumConstants Order))
                                  (map identity (.getEnumConstants Order))))

(def ^:private by-object (sql/map-invert by-keyword))

(defn obj->kw [obj]
  (by-object obj))

(defn kw->obj [unit-type]
  (by-keyword unit-type))