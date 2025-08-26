(ns pepper.game.resources)

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

(defn can-afford?-v2 [budget cost]
  (every? true? (map <= cost budget)))