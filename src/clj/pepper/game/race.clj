(ns pepper.game.race
  (:refer-clojure :exclude [key keys])
  (:require [clojure.set :as sql]
            [clojure.string :as str])
  (:import [bwapi Race]))

(def by-keyword (zipmap (->> (map str (.getEnumConstants Race))
                             (map str/lower-case)
                             (map keyword))
                        (map identity (.getEnumConstants Race))))

(def by-object (sql/map-invert by-keyword))

(defn keyword->object [race]
  (by-keyword race))

(defn object->keyword [race]
  (by-object race))