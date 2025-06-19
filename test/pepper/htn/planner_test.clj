(ns pepper.htn.planner-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [pepper.htn.planner :as planner]))

(deftest test-planner
  (let [add-precondition-fn (fn [task precondition]
                              (update task :task/preconditions conj precondition))

        add-subtask-fn (fn [method subtask]
                         (update method :task/subtasks conj subtask))

        add-method-fn (fn [compound-task method]
                        (update compound-task :task/methods conj method))

        plan-and-execute-fn (fn [state domain]
                              (let [plan (planner/plan state domain)]
                                (planner/execute state plan)))

        tester (planner/task :compound {:task/name :tester
                                        :task/methods []})

        method (planner/task :method {:task/name :method
                                      :task/preconditions []
                                      :task/subtasks []})

        increment-number (planner/task :primitive {:task/name :increment-number
                                                   :task/preconditions []
                                                   :task/effects []
                                                   :task/operator (fn [state] (inc state))})]

    (testing "I can compose and execute a simple plan to increment a number from 1 to 2"
      (let [init-state 1
            goal-state 2
            method (add-subtask-fn method increment-number)
            tester (add-method-fn tester method)]
        (is (= goal-state (plan-and-execute-fn init-state tester)))))

    (testing "I can compose and execute a simple plan to increment a number from 1 to 3 by having 2 primitive incrementing tasks"
      (let [init-state 1
            goal-state 3
            method (add-subtask-fn method increment-number)
            method (add-subtask-fn method increment-number)
            tester (add-method-fn tester method)]
        (is (= goal-state (plan-and-execute-fn init-state tester)))))

    (testing "I can compose and execute a simple plan that will only increment a number if it's smaller than 1"
      (let [init-state 1
            condition-fn (fn [state] (< state 1))
            task (add-precondition-fn increment-number condition-fn)
            method (add-subtask-fn method task)
            tester (add-method-fn tester method)]
        (is (= 1 (plan-and-execute-fn 0 tester)))
        (is (= 1 (plan-and-execute-fn 1 tester)))
        (is (= 2 (plan-and-execute-fn 2 tester)))))

    (testing "I can compose and execute a simple plan that will only increment a number two times if it's smaller than 1"
      (let [init-state 1
            condition-fn (fn [state] (< state 1))
            task (add-precondition-fn increment-number condition-fn)
            method (add-subtask-fn method task)
            method (add-subtask-fn method increment-number)
            tester (add-method-fn tester method)]
        (is (= 2 (plan-and-execute-fn 0 tester)))
        (is (= 1 (plan-and-execute-fn 1 tester)))
        (is (= 2 (plan-and-execute-fn 2 tester)))))

    (testing "The order in which I evaluate subtask preconditions doesn't matter")
    (testing "I can compose and execute a plan with 2 methods, and it will execute only one of those methods")
    (testing "effects are taken into account, and only during planning")))

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