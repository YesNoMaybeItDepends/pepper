(ns pepper.pro
  (:require [pepper.core :as pepper]))

(defonce store (atom {}))

(defn main []
  (pepper/-main store))