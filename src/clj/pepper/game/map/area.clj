(ns pepper.game.map.area)

;; should this actually be navigation? 

(defn filter-area-choke-points-to-target-area [area-from area-to choke-points]
  (let [choke-points-from choke-points
        area-ids #{(:id area-from) (:id area-to)}]
    (filterv #(every? area-ids (:areas %)) choke-points-from)))

(defn choke-points-to-accessible-neighbors [area accessible-neighbors choke-points]
  (->> accessible-neighbors
       (mapv
        #(filter-area-choke-points-to-target-area
          area
          %
          choke-points))
       flatten
       vec))