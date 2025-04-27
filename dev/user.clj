#_{:clj-kondo/ignore [:unused-namespace]}

(ns user
  (:require
   [user.portal :refer [start-portal!]]
   [pepper.core :as core]
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

;; start!
;; stop!