(ns pepper.vpd-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.spec.test.alpha :as st]
   [clojure.spec.alpha :as s]
   [pepper.interop :as i]))

(st/instrument)
(s/check-asserts true)

#_"db"
(let [db {:db/players-by-id {1 {:player/id 1
                                :player/name "Neutral"
                                :player/neutral? true
                                :player/units [1 2]}
                             2 {:player/id 2
                                :player/name "Jimmy"
                                :player/self? true
                                :player/units [3 4]}
                             3 {:player/id 3
                                :player/name "Shadow Jimmy"
                                :player/units [5 6]}
                             4 {:player/id 4
                                :player/name "Clone Jimmy"
                                :player/units [5 6]}}
          :db/forces-by-id {1 {:force/name "Jimbo's Jimbers"
                               :force/players [2 4]}
                            2 {:force/name "Jimbered Jimbos"
                               :force/players [3]}}
          :db/players-by-force {1 [2 4]
                                2 [3]}

          :db/players-by-kw {::self 2
                             ::enemy [2]
                             ::ally [4]
                             ::neutral [1]}
          :db/units-by-id {1 {:unit/id 1
                              :unit/player 1 ;; which do I prefer ?
                              :player/id 1   ;; which do I prefer ?
                              :unit/type :unit-type/minerals}
                           2 {:unit/id 2
                              :player/id 1
                              :unit/type :unit-type/minerals}}
          :db/units-by-type {::minerals [1 2]
                             ::scv []}
          :db/units-by-player {1 [1 2]
                               2 []}}

      #_unit-by-id #_(fn [db id]
                       (get-in db [:db/units-by-id id]))
      #_units-by-type #_(fn [db type]
                          (get-in))
      walk (fn [db path]
             (reduce
              (fn [data p]
                (cond
                  ;; (ifn? p) (p data)
                  (keyword? p) (p data)
                  (number? p) ()))
              db
              path))])

#_(let [a [1 2 3]
        b {1 {:id 1}
           2 {:id 2}
           3 {:id 3}}
        path 1
        f (fn [x p]
            (reduce
             (fn [x p]
               (cond
                 (map? x) (p x)
                 (vector? x) "idk what i was thinking"))
             x
             p))]
    (f a path))

#_([1 2 3] 1)
;; i was here 
(let [db {:units {1 {:id 1} 2 {:id 2} 3 {:id 3}}
          :forces {1 {:id 1 :units [1 2]} 2 {:id 2 :units [3]}}}
      p [:forces 1 :units]]
  (-> db
      (get :forces) ;;=> {1 {:id 1, :units [1 2]}, 2 {:id 2, :units [3]}}
      (get 1) ;;=> {:id 1, :units [1 2]}
      (get :units) ;;=> [1 2]
      #_())
  (let [db {:units {1 {:id 1} 2 {:id 2}}}
        acc-2 {:id 1, :units [1 2]}
        acc-1 [1 2]
        curr-2 :units
        curr-1 nil]
    (if-let [x (get db curr-2)]
      (map #(get x %) acc-1)
      acc-1)))



({1 {:hello :world}} 1)
({:something {:hello :world}} :something)
[:forces 0 :units]
[{0 {:id 0 :units []} 1 {}} 0 :units]
[{:id 0 :units [1 2 3]} :units]
[[1 2 3] :units]

[[:forces 0] :units]
[{#_force_1} [:units]]


[:force 0 :units 1]
[:force 0 :units :scv]


(defn unit-by-id [id])
#_""

[:units]

[[:units :scv] [:player :teal]]

{:game {:players {1 {:units {1 []}}}}}

{:unit/id 1
 :unit/type :scv}
[:units {:by-id {}
         :by-type {:scv []}}]

{:units {1 {}
         2 {}}}

#_(defn vpd [path]
    (get-in))