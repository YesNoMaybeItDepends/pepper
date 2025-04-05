(ns pepper.bwapi.unit-type
  "See https://javabwapi.github.io/JBWAPI/bwapi/UnitType.html"
  (:require [clojure.string :as str])
  (:import (bwapi UnitType)))

(defn supply-provided
  [unit-type]
  {:pre [(some? unit-type)]}
  (.supplyProvided unit-type))

(defn supply-required
  [unit-type]
  {:pre [(some? unit-type)]}
  (.supplyRequired unit-type))

(defn is-building?
  [unit-type]
  {:pre [(some? unit-type)]}
  (.isBuilding unit-type))

(defn is-worker?
  [unit-type]
  {:pre [(some? unit-type)]}
  (.isWorker unit-type))

(defn builds-what
  [unit-type]
  {:pre [(some? unit-type)]}
  (.buildsWhat unit-type))

;; helpers, maybe move somewhere else?

(def ^:private enum-constants
  (.getEnumConstants UnitType))

(defn- keywordize-enum [enum]
  (-> (.name enum)
      (str/lower-case)
      (str/replace #"^[^_]*_" "")
      (str/replace #"_" "-")
      (keyword)))

(def ^:private by-kw
  (reduce (fn [into enum]
            (assoc into (keywordize-enum enum) enum))
          {} enum-constants))

(def ^:private by-enum
  (reduce (fn [into enum]
            (assoc into enum (keywordize-enum enum))) {} enum-constants))

(def keywords (keys by-kw))

(defn kw->enum [kw]
  (get by-kw kw))

(defn enum->kw [enum]
  (get by-enum enum))

(defn valid-kw? [kw]
  (contains? by-kw kw))

