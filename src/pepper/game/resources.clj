(ns pepper.game.resources
  (:require [pepper.game.player :as player]))

(defn init-resources
  "TODO: resources could be the AVAILABLE [minerals gas supply]
   for supply, that would be total - used"
  [state]
  (assoc state :resources {:gas 0
                           :minerals 0
                           :supply [0 0]}))

(defn quantity
  "A resource tuple
   
   Can be `[minerals gas]`

   Or `[minerals gas supply]` (units also cost supply)"
  ([minerals gas] [minerals gas])
  ([minerals gas supply] [minerals gas supply]))

(defn supply [[used total]]
  {:supply-used used
   :supply-total total})

(defn sum-quantities [a b]
  (mapv + a b))

(defn combine-quantities [operand a b]
  (mapv operand a b))

(defn multiply-quantity [quantity by]
  (mapv #(* by %) quantity))

(defn cost [minerals gas supply]
  [minerals gas supply])

(defn get-state-resources [state]
  (:resources state))

(defn get-minerals [resources]
  (:minerals resources))

(defn get-gas [resources]
  (:gas resources))

(defn get-supply [resources]
  (:supply resources))

(defn supply-capped? [[_ total]]
  (= 400 total))

(defn supply-blocked? [[used total]]
  (>= used total))

(defn supply->supply-used [[used _]]
  used)

(defn supply->supply-total [[_ total]]
  total)

(defn supply->supply-available [[used total]]
  (- total used))

(defn resources->resource-tuple [resources]
  [(get-minerals resources)
   (get-gas resources)
   (supply->supply-available (get-supply resources))])

(defn get-state-resources-available [state]
  (-> (get-state-resources state)
      resources->resource-tuple))

(defn- get-our-resources [state]
  (let [p (player/get-self state)]
    {:minerals (player/minerals p)
     :gas (player/gas p)
     :supply [(player/supply-used p) (player/supply-total p)]}))

(defn- update-resources [state resources]
  (assoc state :resources resources))

(defn process-resources [state]
  (update-resources state (get-our-resources state)))

(defn can-afford?
  "please stop using this"
  [state cost]
  (let [have (-> state
                 get-state-resources
                 resources->resource-tuple)]
    (every? true? (map <= cost have))))

(defn can-afford?-v2 [budget cost]
  (every? true? (map <= cost budget)))