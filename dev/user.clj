#_{:clj-kondo/ignore [:unused-namespace]}

(ns user
  (:require [user.portal :refer [start-portal! #_stop-portal!]]
            [pepper.core :as pepper]
            [portal.api :as p]
            [clojure.reflect :as reflect]
            [clojure.core.async :as async]
            [clojure.java.process :as process]
            [clojure.java.io :as io]
            [clojure.repl :as repl]
            [pepper.client :as client]
            [clojure.pprint :as pprint]
            [zprint.zprint :as zp]
            [clojure.spec.alpha :as s]
            [pepper.starcraft :as starcraft]))

(defonce portal (atom (start-portal!)))
#_(swap! portal stop-portal!)

(defn start-client [] (pepper/start-client))
(defn start-game [] (pepper/start-game))
(defn stop-game [] (pepper/stop-game))
(defn stop-client [] (pepper/stop-client))

(defn run []
  (try
    (start-client)
    (start-game)
    (catch Exception e (println e))))

(defn stop []
  (try (stop-game)
       (stop-client)
       (shutdown-agents)
       (catch Exception e (println e))))

(when false
  (starcraft/stop!)
  (starcraft/start!)
  #_())

(comment

  (run)
  (stop)

  #_())