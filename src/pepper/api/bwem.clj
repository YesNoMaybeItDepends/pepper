(ns pepper.api.bwem
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
    (switch-errors! b :off)
    (BWEM/.initialize b)
    (switch-errors! b :on)))