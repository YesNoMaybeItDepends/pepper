(ns pepper.procs.handler
  (:require [clojure.core.async.flow :as flow]
            [pepper.api.game :as game]))

(defn describe [] {:params {:game "bwapi game"
                            :from-game "ch from game event callbacks"
                            :to-game "ch to game event callbacks"}
                   :ins {:request "requests to game"}
                   :outs {:out "output channel"}
                   #_:workload #_:compute})

(defn init
  [{:keys [from-game to-game game] :as args}]
  (assoc args
         :game game
         :request []
         ::flow/in-ports {:from-game from-game}
         ::flow/out-ports {:to-game to-game}))

(defn transition
  [{:keys [client
           client-config] :as state}
   transition]
  state)

(defn handle-game-request [game {:keys [pid inid request] :as message}]
  (when (fn? request) {[pid inid] [(request game)]}))

(defn transform [{:keys [game request] :as state} id msg]
  (case id
    :request [(assoc state :request msg) nil]
    :from-game (let [req (handle-game-request game request)]
                 [(assoc state :request nil)
                  {:to-game ["DONE"]
                   :out (filterv some? [seq])}])))

(defn proc
  ([] (#'describe))
  ([args] (#'init args))
  ([state lifecycle] (#'transition state lifecycle))
  ([state id msg] (#'transform state id msg)))

(defn test-draw-text
  "Assumes pid :handler with inid :request"
  [f]
  (let [msg {:pid :test
             :inid :in
             :request (game/with-game game/draw-text-screen
                        100 100 (str (inst-ms (java.time.Instant/now))))}]
    (flow/inject f [:handler :request] [msg])))

#_(game 100 100 (str (game/get-frame-count game)))