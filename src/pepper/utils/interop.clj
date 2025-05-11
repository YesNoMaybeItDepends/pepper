(ns pepper.utils.interop
  (:require [clojure.java.data :as j]
            [clojure.string :as str]))

(defn- kebabify-property
  [string]
  (let [regex #"(?<![A-Z])[A-Z](?![A-Z])|(?<=[^A-Z:])[A-Z](?=[A-Z])|(?<=[A-Z])[A-Z](?![A-Z])(?=[a-z])"]
    (str/replace string regex #(str "-" %1))))

(defn- property->keyword
  [property]
  {:property property
   :keyword (-> property
                name
                kebabify-property
                str/lower-case
                keyword)})

(defn- obj->bean [obj]
  (j/from-java-shallow obj {}))

(defn- bean->map
  [bean]
  (update-keys bean (:keyword property->keyword)))

(defn- bean->keys
  [bean]
  (map property->keyword (keys bean)))

(defn obj->keys
  [obj]
  (->> obj
       obj->bean
       bean->keys))

(defn obj->map
  [obj]
  (-> obj->bean
      bean->map))