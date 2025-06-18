(ns pepper.htn.planner-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [pepper.htn.planner :as planner]))

(deftest test-planner
  (testing "I can compose and execute a simple plan to increment a number from 1 to 2"
    (let [init-state 1
          goal-state 2
          primitive-increment (planner/task :primitive {:task/name :compound-increment
                                                        :task/preconditions []
                                                        :task/effects []
                                                        :task/operator (fn [state] (inc state))})
          method-increment (planner/task :method {:task/name :method-increment
                                                  :task/preconditions []
                                                  :task/subtasks [primitive-increment]})
          compound-increment (planner/task :compound {:task/name :compound-increment
                                                      :task/methods [method-increment]})
          plan-increment (planner/plan init-state compound-increment)
          end-state (planner/execute init-state plan-increment)]
      (is (= goal-state end-state)))))

#_(let [from-state {:db/items #{{:item/id 1
                                 :item/name :axe}}
                    :world/trees 1
                    :world/items-by-id #{1}
                    :player/items-by-id #{}}

        to-state {:db/items #{{:item/id 1
                               :item/name :axe}}
                  :world/trees 0
                  :world/items-by-id #{}
                  :player/items-by-id #{1}}

        axe-id (-> (game/first-by-kv [:item/name :axe] (:db/items from-state))
                   :item/id)
        #_()]

    (is (= to-state (-> from-state
                        (game/transfer-id-fn axe-id :world/items-by-id :player/items-by-id)
                        game/chop-tree)))

    (is (false? (game/can-chop-tree from-state)))
    (is (false? (game/can-chop-tree to-state)))
    (is (true? (game/can-chop-tree (-> from-state
                                       (game/transfer-id-fn axe-id :world/items-by-id :player/items-by-id))))))