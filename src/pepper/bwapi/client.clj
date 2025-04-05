(ns pepper.bwapi.client
  "See https://javabwapi.github.io/JBWAPI/bwapi/BWClient.html")

(defn get-game [client]
  (.getGame client))