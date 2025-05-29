(ns pepper.utils.repl
  (:require [nrepl.server :as nrepl]
            [cider.nrepl :as cider]))

(def server (atom nil))

(defn start-server!
  "now 7888, maybe 55808 ? or 8080 !"
  []
  (let [s (nrepl/start-server :port 7888 :handler cider/cider-nrepl-handler)]
    (reset! server s)))

(defn stop-server! []
  (let [s (nrepl/stop-server @server)]
    (reset! server nil)))