(ns pepper.api.bwem
  (:require [com.brunobonacci.mulog :as mu])
  (:import [bwem BWEM]))

(defn switch-errors! [b toggle]
  (let [[fail? stream] (if (= toggle :off)
                         [false nil]
                         [true System/err])]
    (BWEM/.setFailOnError b fail?)
    (BWEM/.setFailOutputStream b stream))
  b)

(defn init! [game]
  (let [b (new bwem.BWEM game)]
    (mu/log :initializing-bwem)
    (switch-errors! b :off)
    (BWEM/.initialize b)
    (mu/log :initialized-bwem)
    (switch-errors! b :on)))