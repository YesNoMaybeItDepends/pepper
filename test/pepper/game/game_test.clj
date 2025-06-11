(ns pepper.game.game-test
  (:require
   [clojure.test :refer [deftest is]]
   [pepper.game.game :as game]
   [pepper.htn.task :as task]
   [pepper.htn.planner :as planner]))



(deftest test-chop-trees
  (let [state {}
        state2 {:world/trees 0}]
    (is (= state2 (game/chop-tree state2)))))

(let [chop-tree (task/primitive {:name :chop-tree
                                 :preconditions [game/can-chop-tree]
                                 :effects [game/chop-tree]})
      get-axe (task/primitive {:name :get-axe
                               :preconditions [(complement game/has-axe)]
                               :effects [game/get-axe]})
      minecraft (task/compound {:name :minecraft
                                :methods [chop-tree get-axe]})
      from-state {:db/items #{{:item/id 1
                               :item/name :axe}}
                  :world/trees 1
                  :world/items-by-id #{1}
                  :player/items-by-id #{}}

      iterate-fn (fn [task state]
                   (-> (planner/plan task state)
                       (planner/execute state)))

      simulate-fn (fn [times state task]
                    (map-indexed
                     hash-map
                     (take times
                           (iterate
                            #((partial iterate-fn task) %)
                            state))))

      results (simulate-fn 4 from-state minecraft)]
  results)