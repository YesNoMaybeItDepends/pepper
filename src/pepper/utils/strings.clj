(ns pepper.utils.strings
  (:require
   [clojure.string :as str]))

(defn camel->kebab
  [string]
  (let [regex #"(?<![A-Z])[A-Z](?![A-Z])|(?<=[^A-Z:])[A-Z](?=[A-Z])|(?<=[A-Z])[A-Z](?![A-Z])(?=[a-z])"]
    (str/replace string regex #(str "-" %1))))