#_{:clj-kondo/ignore [:unused-namespace]}

(ns user
  (:require
   [user.portal :refer [start-portal!]]
   [portal.api :as p]
   [clojure.reflect :as reflect]
   [clojure.core.async :as async]
   [clojure.java.process :as process]
   [clojure.java.io :as io]
   [clojure.repl :as repl]
   [clojure.pprint :as pprint]
   [zprint.zprint :as zp]
   [clojure.spec.alpha :as s]
   [pepper.core :as pepper]
   [flow-storm.api :as fs-api]))

(defonce portal (atom (start-portal!)))
(defonce flowstorm (do (fs-api/local-connect)
                       true))

(comment

  (pepper/-main)

  #_())
