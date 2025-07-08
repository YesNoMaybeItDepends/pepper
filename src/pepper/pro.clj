(ns pepper.pro
  (:require
   [pepper.core :as pepper]
   [pepper.systems.logging :as logging]))

(defonce store (atom {}))

(defn main []
  (logging/init-logging)
  (pepper/main store))