(ns pepper.api.unit-type
  "helpers, maybe move somewhere else?"
  (:require [clojure.string :as str])
  (:import (bwapi UnitType)))

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

(defn kw->type [kw]
  (get by-kw kw))

(defn type->kw [enum]
  (get by-enum enum))

(defn valid-kw? [kw]
  (contains? by-kw kw))