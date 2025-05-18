(ns pepper.procs.game-events
  (:require [clojure.set :as sql]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as g]
            [clojure.string :as string]))

(defn qualify-key [k]
  (keyword (str *ns*) (name k)))

(defn valid? [{:keys [event data] :as input-event}]
  (boolean event))

(def game-event? #{::on-end #_{:is-winner isWinner}
                   ::on-frame
                   ::on-nuke-detect #_{:target target}
                   ::on-player-dropped #_{:player player}
                   ::on-player-left #_{:player player}
                   ::on-receive-text #_{:player player :text text}
                   ::on-save-game #_{:gameName gameName}
                   ::on-send-text #_{:text text}
                   ::on-start
                   ::on-unit-complete #_{:unit unit}
                   ::on-unit-create #_{:unit unit}
                   ::on-unit-destroy #_{:unit unit}
                   ::on-unit-discover #_{:unit unit}
                   ::on-unit-evade #_{:unit unit}
                   ::on-unit-hide #_{:unit unit}
                   ::on-unit-morph #_{:unit unit}
                   ::on-unit-renegade #_{:unit unit}
                   ::on-unit-show #_{:unit unit}})

(def in? #{::game-event})
(def out? (sql/union game-event?
                     #{::on-any}))

(defn describe [] {:ins {::game-event "Any game event"}
                   :outs {::on-any "Any game event"
                          ::on-start "At game start"
                          ::on-frame "On frame"}})

(defn init [arg-map] (assoc arg-map :events []))

(defn transition [state transition] state)

(defn transform [state input msg]
  (case input ::game-event
        (let [state (update state :events conj (merge {:id (count (:events state))} msg))
              outs {::on-any [msg]}
              #_outs #_(merge {::on-any [msg]}
                              {(qualify-key (:event msg)) [msg]})]
          [state outs])))
#_(transform {} ::game-event {:event :on-frame})
(defn proc
  ([] (#'describe))
  ([arg-map] (#'init arg-map))
  ([state _transition] (#'transition state _transition))
  ([state input msg] (#'transform state input msg)))