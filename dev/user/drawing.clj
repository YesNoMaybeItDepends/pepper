(ns user.drawing
  (:require
   [quil.core :as q]
   [quil.middleware :as qm]))

(defn line-height []
  (+ (q/text-ascent)
     (q/text-descent)))

(defn setup-state []
  {:frame-rate 1
   :x 0
   :y 0
   :count 0
   :background-color (q/color 255 255 255)
   :stroke-color (q/color 255 0 0)
   :stroke-weight 3
   :text-color (q/color 0 0 0)
   :text-size 10})

(defn update-state [state]
  (-> state
      (update :x inc)
      (update :y inc)
      (update :count inc)))

(defn draw-state [state]
  (q/frame-rate (:frame-rate state))
  (q/background (:background-color state))

  (let [{:keys [stroke-color x y]} state]
    (q/with-stroke stroke-color
      (q/stroke-weight 5)
      (q/point x y)))

  (let [{:keys [text-color text-size count]} state]
    (q/with-fill text-color
      (q/text "woah!!!!" 50 50)
      (q/text-num count 0 (line-height))
      (q/text-num count 0 (* 2 (line-height))))))

(defn sketch []
  (q/sketch :title "woah"
            :setup #'setup-state
            :update #'update-state
            :draw #'draw-state
            :features [:keep-on-top]
            :middleware [qm/fun-mode]))