(ns pepper.bot.our
  (:require
   [pepper.api :as api]
   [pepper.game :as game]
   [pepper.game.player :as player]
   [pepper.game.unit :as unit])
  (:import
   [bwapi Game Player]))

;; {:player/id 0
;;  :units #{0 1 2 3}
;;  :bases #{[42 42]}
;;  :resources [0 0 0]}

(defn set-player-id [our id]
  (assoc our :player/id id))

(defn player-id [our]
  (:player/id our))

(defn player [our game]
  (get (game/players-by-id game) (player-id our)))

(defn starting-base [our game]
  (player/starting-base (player our game)))

(defn our-unit? [unit our-id]
  (= our-id (unit/player-id unit)))

(defn units [our game]
  (->> (vals (game/units-by-id game))
       (filterv #(our-unit? % (player-id our)))))

(defn grouped-units [our]
  (:units our))

(defn units-by-id [our game]
  (->> (units our game)
       (reduce (fn [m u]
                 (assoc m (unit/id u) u))
               {})))

(defn parse-on-start [api]
  (let [game (api/get-game api)]
    {:player/id (Player/.getID (Game/.self game))}))

(defn update-on-start [{:as our :or {}} data]
  (set-player-id our (player-id data)))

(defn update-on-frame [[our messages] game]
  (let [group-units-by [:idle? :type]
        our (assoc our :units (unit/group-units-by-keywords
                               (units our game)
                               group-units-by))]
    [our messages]))