(ns pepper.mocking
  (:import (org.mockito Mockito)
           (bwapi Game Unit UnitType)))

(defn when-then [x w t]
  (-> (w x)
      (Mockito/when)
      (.thenReturn t)
      (.getMock)))

(defn mock [class opts]
  (reduce (fn [acc [m v]]
            (when-then acc m v))
          (Mockito/mock class)
          opts))

(defn mock-unit [id]
  (mock Unit [[#(Unit/.getID %) (int id)]
              [#(Unit/.isFlying %) true]
              [#(Unit/.getType %) UnitType/Terran_SCV]]))

(defn mock-game [units]
  (mock Game [[#(Game/.getAllUnits %) units]]))

(def obj->key-value-map
  [[:unit/id Unit/.getID]
   [:unit/type Unit/.getType]
   [:unit/is-flying Unit/.isFlying]])


(let [u (mock-unit 3)]
  (reduce (fn [acc [kw m]]
            (assoc acc kw (m u)))
          {}
          obj->key-value-map))