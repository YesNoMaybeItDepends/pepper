(ns pepper.htn.planner-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.spec.test.alpha :as st]
   [clojure.spec.alpha :as s]
   [pepper.htn.planner :as planner]
   [clojure.string :as str]))

(st/instrument)
(s/check-asserts true)

(defn plan-and-execute [domain state]
  (let [plan (planner/plan state domain)]
    (planner/execute state plan)))

(defn simulate-planner [state domain times]
  (into []
        (flatten
         (take times
               (iterate
                #((partial plan-and-execute domain) %)
                state)))))

(deftest test-planner
  (let [add-operator-fn (fn [primitive operator]
                          (assoc primitive :task/operator operator))

        add-precondition-fn (fn [task precondition]
                              (update task :task/preconditions conj precondition))

        add-effect-fn (fn [task effect]
                        (update task :task/effects conj effect))

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
        primitive (planner/task :primitive {:task/name :primitive
                                            :task/preconditions []
                                            :task/effects []
                                            :task/operator identity})

        task-increment-number (add-operator-fn primitive inc)
        task-even (add-operator-fn primitive (fn [state] :even))
        task-uneven (add-operator-fn primitive (fn [state] :uneven))
        method-even (-> (add-precondition-fn method even?)
                        (add-subtask-fn task-even))
        method-uneven (-> (add-precondition-fn method (complement even?))
                          (add-subtask-fn task-uneven))]

    (testing "I can compose and execute a simple plan to increment a number from 1 to 2"
      (let [init-state 1
            goal-state 2
            method (add-subtask-fn method task-increment-number)
            tester (add-method-fn tester method)]
        (is (= goal-state (plan-and-execute-fn init-state tester)))))

    (testing "I can compose and execute a simple plan to increment a number from 1 to 3 by having 2 primitive incrementing tasks"
      (let [init-state 1
            goal-state 3
            method (add-subtask-fn method task-increment-number)
            method (add-subtask-fn method task-increment-number)
            tester (add-method-fn tester method)]
        (is (= goal-state (plan-and-execute-fn init-state tester)))))

    (testing "I can compose and execute a simple plan that will only increment a number if it's smaller than 1"
      (let [init-state 1
            condition-fn (fn [state] (< state 1))
            task (add-precondition-fn task-increment-number condition-fn)
            method (add-subtask-fn method task)
            tester (add-method-fn tester method)]
        (is (= 1 (plan-and-execute-fn 0 tester)))
        (is (= 1 (plan-and-execute-fn 1 tester)))
        (is (= 2 (plan-and-execute-fn 2 tester)))))

    (testing "preconditions can just be keywords"
      (let [task (add-precondition-fn (add-operator-fn
                                       primitive
                                       (fn [state] (update state :n inc)))
                                      :hello)
            method (add-subtask-fn method task)
            tester (add-method-fn tester method)]
        (is (= {:hello true :n 1} (plan-and-execute-fn {:hello true :n 0} tester)))
        (is (not= {:hello true :n 0} (plan-and-execute-fn {:hello true :n 0} tester)))))

    (testing "I can compose and execute a simple plan that will only increment a number two times if it's smaller than 1"
      (let [init-state 1
            condition-fn (fn [state] (< state 1))
            task (add-precondition-fn task-increment-number condition-fn)
            method (add-subtask-fn method task)
            method (add-subtask-fn method task-increment-number)
            tester (add-method-fn tester method)]
        (is (= 2 (plan-and-execute-fn 0 tester)))
        (is (= 1 (plan-and-execute-fn 1 tester)))
        (is (= 2 (plan-and-execute-fn 2 tester)))))

    (testing "effects are taken into account, and only during planning"
      (let [condition-fn (fn [state] (< state 1))
            effect-fn inc
            task-1 (-> task-increment-number
                       (add-precondition-fn condition-fn)
                       (add-effect-fn effect-fn))
            task-2 (-> task-increment-number
                       (add-precondition-fn condition-fn))
            method (-> method
                       (add-subtask-fn task-1)
                       (add-subtask-fn task-2))
            tester (add-method-fn tester method)]
        (is (= 0 (plan-and-execute-fn 0 tester)))))

    (testing "I can compose and execute a plan with 2 methods, and it will execute only one of those methods"
      (let [tester (-> (add-method-fn tester method-even)
                       (add-method-fn method-uneven))]
        (is (= :even (plan-and-execute-fn 0 tester)))
        (is (= :uneven (plan-and-execute-fn 1 tester)))))

    (testing "executing a plan will __throw__ if the state upon which the plan is being executed is no longer valid for any of the plan tasks"
      (let [condition-fn (fn [state] (< state 1))
            task (add-precondition-fn task-increment-number condition-fn)
            method (add-subtask-fn method task)
            tester (add-method-fn tester method)
            plan (planner/plan 0 tester)]
        (is (thrown? Exception (planner/execute 2 plan)))
        #_(is (= 2 (planner/execute 2 plan)))))

    (testing "plans have an MTR"
      (let [tester (-> (add-method-fn tester method-even)
                       (add-method-fn method-uneven))
            plan-even (planner/plan 0 tester)
            plan-uneven (planner/plan 1 tester)]
        (is (= [0 0 0] (:mtr (first plan-even))))
        (is (= [0 1 0] (:mtr (first plan-uneven))))))

    (testing "compound tasks can be nested"
      (let [state {:location :starting-location
                   :can-see-enemy false
                   :times-recovered 0
                   :bridge-to-check nil
                   :super-slam-ready false}
            nav-to-enemy {:task/name :nav-to-enemy
                          :task/preconditions [:can-see-enemy]
                          :task/effects [(fn [s] (assoc s :location :enemy-location))]
                          :task/operator (fn [s] (assoc s :location :enemy-location))}
            recover {:task/name :recover
                     :task/preconditions []
                     :task/effects [(fn [s] (update s :times-recovered inc))]
                     :task/operator (fn [s] (update s :times-recovered inc))}
            choose-bridge {:task/name :choose-bridge
                           :task/preconditions []
                           :task/effects [(fn [s] (assoc s :bridge-to-check 1))]
                           :task/operator (fn [s] (assoc s :bridge-to-check 1))}
            nav-to-bridge {:task/name :nav-to-bridge
                           :task/preconditions []
                           :task/effects [(fn [s] (assoc s :location :bridge-location))]
                           :task/operator (fn [s] (assoc s :location :bridge-location))}
            check-bridge {:task/name :check-bridge
                          :task/preconditions []
                          :task/effects [(fn [s] (assoc s :can-see-enemy true))]
                          :task/operator (fn [s] (assoc s :can-see-enemy true))}
            taunt {:task/name :taunt
                   :task/preconditions []
                   :task/effects []
                   :task/operator identity}
            super-slam {:task/name :super-slam
                        :task/preconditions []
                        :task/effects []
                        :task/operator identity}
            tell {:task/name :tell
                  :task/preconditions []
                  :task/effects []
                  :task/operator identity}
            slam {:task/name :slam
                  :task/preconditions []
                  :task/effects []
                  :task/operator identity}
            do-trunk-slam {:task/name :do-trunk-slam
                           :task/methods [{:task/name :do-trunk-slam-1
                                           :task/preconditions [:super-slam-ready]
                                           :task/subtasks [taunt super-slam]}
                                          {:task/name :do-trunk-slam-2
                                           :task/preconditions []
                                           :task/subtasks [tell slam]}]}
            be-trunk-thumper {:task/name :be-trunk-thumper
                              :task/methods [{:task/name :be-trunk-thumper-1
                                              :task/preconditions [:can-see-enemy]
                                              :task/subtasks [nav-to-enemy do-trunk-slam recover]}
                                             {:task/name :be-trunk-thumper-2
                                              :task/preconditions []
                                              :task/subtasks [choose-bridge nav-to-bridge check-bridge]}]}
            domain (planner/task :compound be-trunk-thumper)
            states (simulate-planner state domain 3)]
        (is (= :starting-location (:location (states 0))))
        (is (= :bridge-location (:location (states 1))))
        (is (= :enemy-location (:location (states 2))))
        (is (= 1 (:times-recovered (states 2))))))))