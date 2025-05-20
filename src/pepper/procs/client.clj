(ns pepper.procs.client
  (:require
   [clojure.core.async :as a]
   [clojure.core.async.flow :as flow]
   [clojure.spec.alpha :as s]
   [pepper.api.client :as c]
   [pepper.api.game :as g]))

(defn describe
  []
  {:params {:client-config "client config"}
   :ins {:with-game "anything to do with the game"}
   :outs {:out "blabla"}})

(defn init
  [{:keys [client-config] :as args}]
  (let [game-events-ch (a/chan (a/sliding-buffer 1))
        client (c/client (partial a/put! game-events-ch))]
    (assoc args
           :client client
           :client-config client-config
           ::flow/in-ports {:client-events game-events-ch})))

(defn transition
  [{:keys [client client-config] :as state} transition]
  (case transition
    ::flow/resume (do (future (c/start-game client (c/configuration client-config)))
                      state)))

(defn transform
  [{:keys [client game] :as state} id msg]
  (case id
    :client-events (case (:event msg)
                     :on-start (do (println "on start")
                                   (let [game (c/get-game client)]
                                     [(assoc state :game game) nil]))

                     :on-frame (do (println "on frame")
                                   (g/draw-text-screen game 100 100 (str (g/get-frame-count game)))
                                   [state nil])

                     (do (println "on anything else")
                         [state nil]))))

(defn proc
  ([] (#'describe))
  ([args] (#'init args))
  ([state lifecycle] (#'transition state lifecycle))
  ([state id msg] (#'transform state id msg)))