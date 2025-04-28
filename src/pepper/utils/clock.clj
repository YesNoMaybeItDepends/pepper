(ns pepper.utils.clock "testing something")

(defn update-state
  [state tick]
  (cond
    :else state))

(def starting-clock {:tick 0
                     :max-tick 10
                     :ms 1000
                     :state {:supply-used 0
                             :supply-total 0}
                     :on-tick update-state})

(defn tick-clock
  [{ms :ms
    tick :tick
    state :state
    on-tick :on-tick
    :as clock}]

  (Thread/sleep ms)
  (assoc clock :state (on-tick state tick) :tick (inc tick)))

(defn tick-clock?
  [{tick :tick max-tick :max-tick}]
  (< tick max-tick))

(defn clock
  [starting-clock]
  (loop [clock starting-clock]
    (when (tick-clock? clock)
      (println clock)
      (recur (tick-clock clock)))))