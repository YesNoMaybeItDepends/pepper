(ns pepper.procs.game-state
  (:require
   [clojure.spec.alpha :as s]
   [pepper.api.game :as game]
   [pepper.bw.player :as player]
   [pepper.bw.unit :as unit]
   [pepper.utils.time :refer [timestamp]]
   [clojure.java.data :as j]
   [portal.api :as portal]))

(s/def state? #{::start-frame
                ::start-inst
                ::start-time
                ::last-frame
                ::last-inst
                ::last-time
                ::is-paused
                ::players-by-id
                ::player-id
                ;;;; Maybe?
                ::units-by-id
                #_::locations-by-id
                #_::buildings-by-id
                #_::forces-by-id
                #_::geysers-by-id
                #_::minerals-by-id})

(defn make-idle-scvs-mine [game {:keys [::player-id] :as state}]
  (let [units (vals (::units-by-id state))
        my-units (filter (partial unit/owned-by-player? player-id) units)
        my-idle-units (filter :idle? my-units)
        my-idle-scvs (filter (partial unit/type? :scv) my-idle-units)]
    (when (seq my-idle-scvs)
      (let [minerals (filter (partial unit/type? :mineral-field) units)
            minerals (map (fn [unit] (game/get-unit game (:id unit))) minerals)
            scvs (map (fn [unit] (game/get-unit game (:id unit))) my-idle-scvs)]
        (doseq [scv scvs]
          (tap> "telling some scv to mine")
          (.gather scv (first minerals)))))))

(defn update-on-start
  [state {:keys [game] :as msg}]
  (tap> "update-on-start")
  (tap> msg)
  #_(game/set-local-speed game :slowest)
  (assoc state
         ::start-frame (game/get-frame-count game)
         ::start-inst (timestamp)
         ::start-time (game/elapsed-time game)
         ::last-frame (game/get-frame-count game)
         ::last-inst (timestamp)
         ::last-time (game/elapsed-time game)
         ::is-paused (game/is-paused game)
         ::players-by-id (player/players-by-id game)
         ::player-id (.getID (game/self game))
         ::units-by-id (unit/units-by-id (game/get-all-units game))))

(defn update-on-frame
  [state {:keys [game] :as msg}]
  (game/draw-text-screen game 10 10 (str (::last-time state)))
  (make-idle-scvs-mine game state)
  (assoc state
         ::last-frame (game/get-frame-count game)
         ::last-inst (timestamp)
         ::last-time (game/elapsed-time game)
         ::is-paused (game/is-paused game)
         ::units-by-id (unit/units-by-id (game/get-all-units game))))

;;;; Flow

(def out? #{::out})
(def in? #{::game-event})

(defn describe []
  {:ins {::game-event "Any game event"}
   :outs {::out "Anything"}})

(defn init
  [args]
  (assoc args
         :init-inst (timestamp)))

(defn transition [state transition] state)

(defn transform
  [state input msg]
  (case input
    ::game-event (case (:event msg)
                   :on-start [(#'update-on-start state msg) {::out [state]}]
                   :on-frame [(#'update-on-frame state msg) {::out [state]}])))

(defn proc
  ([] (#'describe))
  ([args] (#'init args))
  ([state _transition] (#'transition state _transition))
  ([state input msg] (#'transform state input msg)))