(ns pepper.api.bwem
  (:require [pepper.utils.logging :as logging])
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
    (logging/log {:event :initializing-bwem})
    (switch-errors! b :off)
    (BWEM/.initialize b)
    (logging/log {:event :initialized-bwem})
    (switch-errors! b :on)))