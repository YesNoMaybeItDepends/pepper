(ns pepper.macro.budget
  (:require
   [clojure.spec.alpha :as s]
   [pepper.bwapi.impl.game :as game]
   [pepper.bwapi.player :as player]))

(s/def ::minerals keyword?)
(s/def ::gas keyword?)

(s/def ::total int?)
(s/def ::reserved int?)
(s/def ::available int?)

(s/def ::resource-budget (s/keys :req [::total ::reserved ::available]))
(s/def ::budget (s/map-of #{::minerals ::gas} ::resource-budget))
(s/valid? ::budget {::minerals {::total 1 ::reserved 0 ::available 0} ::gas {::total 1 ::reserved 0 ::available 0}})

(def keywords-budget #{:total :reserved :available})
(def keywords-resources #{:minerals :gas :supply})

(defn keywords->map
  ([ks] (keywords->map ks nil))
  ([ks v] (reduce (fn [map keyword] (assoc map keyword v)) {} ks)))

(defn init-budget
  "Makes a map of keys keywords-budget vals keywords-resources"
  []
  (update-vals (keywords->map keywords-budget) (fn [v] (keywords->map keywords-resources))))

(def budget (atom (init-budget)))

(defn get-budget [] @budget)

(defn update! [budget minerals]
  (update-in budget [:total :minerals] (fn [total-minerals] minerals)))

(defn run-frame [game]
  (let [player (game/self game)
        minerals (player/minerals player)]
    (swap! budget update! minerals)))

(defn initial-budget [] {:minerals 0 :gas 0 :supply 0})

(defn total [] {:minerals nil :gas nil :supply nil})
(defn reserved [] {:minerals nil :gas nil :supply nil})
(defn available [] {:minerals nil :gas nil :supply nil})

(defn total-minerals [n] n)
(defn total-gas [n] n)

(defn reserved-minerals [n] n)
(defn reserved-gas [n] n)

(defn available-minerals [x y] (- (total-minerals x) (reserved-minerals y)))
(defn available-gas [x y] (- (total-gas x) (reserved-minerals y)))

(defn reserve-minerals! [])