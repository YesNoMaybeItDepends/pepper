(ns pepper.api.bwem
  (:import (bwem BWEM)))

(defn set-fail-on-error
  [bwem bool]
  (.setFailOnError bwem bool))

(defn set-fail-output-stream
  [bwem output-stream]
  (.setFailOutputStream bwem output-stream))

(defn instantiate [game]
  (new BWEM game))

(defn init
  [bwem fail-output-stream]
  (set-fail-on-error bwem false)
  (set-fail-output-stream bwem nil)
  (.initialize bwem)
  (set-fail-output-stream bwem System/err) ;; TODO: use fail-output-stream
  (set-fail-on-error bwem true))

(defn get-map
  [bwem]
  (.getMap bwem))