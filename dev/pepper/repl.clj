(ns pepper.repl
  (:require [nrepl.server :as nrepl]
            [cider.nrepl :as cider]))

(def server (atom nil))
(def queue (atom clojure.lang.PersistentQueue/EMPTY))

(defn start-server!
  "now 7888, maybe 55808 ? or 8080 !"
  [ref port]
  (let [s (nrepl/start-server :port port :handler cider/cider-nrepl-handler)]
    (reset! server s)))

(defn stop-server! []
  (let [s (nrepl/stop-server @server)]
    (reset! server nil)))

(defn enqueue! [fn]
  (swap! queue conj fn))

(defn dequeue! []
  (let [[old new] (swap-vals! queue pop)]
    (peek old)))