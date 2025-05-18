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

(defn- obj->bean [obj & {:keys [omit] :or {omit #{}} :as options}]
  (j/from-java-shallow obj options))

(defn- bean->map
  [bean]
  (update-keys bean (:keyword property->keyword)))

(defn- bean->keywords
  [bean]
  (map property->keyword (keys bean)))

(defn obj->keys
  [obj]
  (->> obj
       obj->bean
       bean->keywords))

(defn obj->map
  [obj]
  (-> obj->bean
      bean->map))