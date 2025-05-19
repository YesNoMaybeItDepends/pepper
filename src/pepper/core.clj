(ns pepper.core
  (:import (bwapi Text))
  (:require [pepper.api.client :as client]
            [clojure.core.async :as a]))

(def game-ref (atom nil))
(def client-ref (atom nil))

(defn event-handler [{:keys [this event data] :as msg}]
  (case event
    :on-start (reset! game-ref (.getGame @client-ref))
    ;; else
    (let [g @game-ref]
      (when (some? g)
        (let [f (.getFrameCount g)]
          (println ":frame" f ":event" event)
          (.drawTextScreen g 100 100 (str (.getFrameCount g)) (into-array Text [])))))))

(def ch (a/chan (a/sliding-buffer 1)
                (fn [xf] (fn [_ x] (#'event-handler x) x))))

(reset! client-ref (client/client (partial a/put! ch)))

(defn -main
  "the bwapi client fails with async for some reason"
  []
  (a/go (try (client/start-game @client-ref (client/configuration {:async false
                                                                   :debug-connection true
                                                                   :log-verbosely true}))
             (catch Exception e (println e)))))