(ns user.drawing
  (:require
   [pepper.game.position :as position]
   [pepper.game.unit :as unit]
   [pepper.game.unit-type :as unit-type]
   [quil.core :as q]
   [quil.middleware :as qm]))

(defn line-height []
  (+ (q/text-ascent)
     (q/text-descent)))

(defn ->v2 [val]
  (if (map? val)
    [(:x val) (:y val)]
    val))

(def ->v2-walk-position (comp ->v2 position/_->walk-position))

(defn setup-state [state-pepper]
  (fn actual-quil-setup-fn []
    (q/frame-rate 30)
    {:text-size-default 12
     :text-size-units 2
     :pepper state-pepper}))

(defn update-state [state]
  (-> state
      (update :x inc)
      (update :y inc)
      (update :count inc)))

(defn draw-mini-tiles [state]
  (q/with-stroke
    (q/color 0 0 0)
    (q/stroke-weight 2)
    (doseq [[[x y] {:keys [walkable]}]
            (get-in state [:pepper :game :map :mini-tiles])]
      (when (not walkable)
        (q/rect x y 1 1)))))

(defn draw-map-edges [state]
  (let [[x y] (->v2 (get-in state [:pepper :game :map :map-data :walk-size]))]
    (q/with-stroke
      (q/color 0 0 0)
      (q/no-fill)
      (q/rect 0 0 x y))))

(defn draw-minerals [state]
  (q/with-stroke
    (q/color 0 228 252)
    (q/stroke-weight 1)
    (doseq [[x y] (transduce
                   (comp
                    (filter (unit/initial-type? unit-type/mineral-field))
                    (map (comp ->v2-walk-position :initial-position)))
                   conj
                   []
                   (vals (get-in state [:pepper :game :units-by-id])))]
      (q/rect x y 1 1))))

(defn draw-workers [state]
  (q/with-stroke
    (q/color 0 255 0)
    (q/stroke-weight 1)
    (doseq [u (transduce
               (comp
                (filter (every-pred (unit/type? unit-type/worker)
                                    :position)))
               conj
               []
               (vals (get-in state [:pepper :game :units-by-id])))]
      (let [[x y] ((comp ->v2-walk-position :position) u)
            id (:id u)]
        (q/rect x y 1 1)
        (q/with-fill
          (q/color 0 0 0)
          (q/text-size (:text-size-units state))
          (q/text (str id) (- x 1) (- y 1))
          (q/text-size (:text-size-default state)))))))

(defn draw-choke-points [state]
  (let [draw-geometry (fn [{:keys [geometry]}]
                        (q/with-stroke
                          (q/color 255 0 0)
                          (q/stroke-weight 5)
                          (doseq [[x y] (mapv ->v2-walk-position geometry)]
                            (q/point x y))))
        draw-center (fn [{:keys [center]}]
                      (q/with-stroke
                        (q/color 255 255 255)
                        (q/stroke-weight 5)
                        (let [[x y] (->v2 center)]
                          (q/point x y))))]
    (doseq [choke-point (vals (get-in state [:pepper :game :map :choke-points]))]
      (draw-geometry choke-point)
      (draw-center choke-point))))

(defn draw-bases [state]
  (q/with-stroke
    (q/color 0 50 100 200)
    (q/stroke-weight 1)
    #_(q/no-fill)
    (doseq [[x y] (mapv (comp ->v2-walk-position :center)
                        (vals (get-in state [:pepper :game :map :bases])))]
      (q/rect x y 2 2))))

(defn draw-areas [state]
  (q/with-stroke
    (q/color 33 33 33)
    (q/no-fill)
    (q/stroke-weight 1)
    (doseq [area (vals (get-in state [:pepper :game :map :areas]))]
      (let [{:keys [top-left-tile
                    bottom-right-tile
                    id]} area
            [x1 y1] (->v2-walk-position top-left-tile)
            [x2 y2] (->v2-walk-position bottom-right-tile)
            [w h] [(- x2 x1) (- y2 y1)]]
        #_(q/rect x1 y1 w h) ;; draw area rect
        (q/with-fill
          (q/color 0 0 0)
          (q/text (str id) (- x2 (/ w 2)) (- y2 (/ h 2))))))))

(defn draw-map [state]
  (draw-map-edges state)
  (draw-mini-tiles state)
  (draw-minerals state)
  (draw-areas state)
  (draw-bases state)
  (draw-choke-points state)
  (draw-minerals state)
  (draw-workers state))

(defn draw-state [state]
  (q/text-size (:text-size-default state))
  (q/background 200)
  (draw-map state))

(defn sketch [state-pepper]
  (q/sketch :title "bro"
            :setup (setup-state state-pepper)
            :update #'update-state
            :draw #'draw-state
            :features [:keep-on-top]
            :middleware [qm/fun-mode qm/navigation-2d]
            :settings (fn [] q/no-smooth)
            :size (->v2 (get-in state-pepper [:game :map :map-data :walk-size]))))