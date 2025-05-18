(ns pepper.core
  (:require [pepper.client :as client]
            [clojure.core.async :as a]))

(defn -main
  "the bwapi client fails with async for some reason"
  []
  (let [ch (a/chan (a/sliding-buffer 1)
                   (fn [xf] (fn [_ data] (println data) data)))
        c (client/client (partial a/put! ch))
        fails (future (try (client/start-game c (client/configuration {:async true
                                                                       :debug-connection true
                                                                       :log-verbosely true}))
                           (catch Exception e (println e))))]
    [ch c fails]))

(comment

  (def hello (-main))

  #_())
