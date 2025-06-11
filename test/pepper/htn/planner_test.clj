(ns pepper.htn.planner-test
  (:require
   [clojure.test :refer [deftest is]]
   [pepper.htn.task :as task]
   [pepper.htn.planner :as planner]))

(deftest test-planner
  (let [state-1 1
        state-2 2
        increment-task (task/primitive {:name :increment
                                        :preconditions []
                                        :effects [(fn [state] (inc state))]})
        compound-task (task/compound {:name :test
                                      :methods [#(println "hi world")]})
        #_validation #_(planner/validate-compound-task compound-task)
        plan (planner/plan {:methods [increment-task]}
                           state-1)
        _ (println (planner/execute plan state-1))]
    #_(is (empty? validation))
    (is (= state-2 (planner/execute plan state-1)))))

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