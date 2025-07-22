(ns pepper.game.game-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.test.alpha :as st]
            [pepper.game.game :as game]))

(st/instrument)

(deftest game-test
  (testing "has a frame"
    (is (= 1 (game/frame {:game/frame 1}))))

  (testing "has a frame -1 if there's no frame"
    (is (= -1 (game/frame {}))))

  (testing "has a map of units by id"
    (is (= {1 {}} (game/units-by-id {:game/units-by-id {1 {}}})))
    (is (= {} (game/units-by-id {}))))

  (testing "can get a list of unit ids"
    (is (= '(1 2) (game/units-by-id-ids {:game/units-by-id {1 {} 2 {}}})))
    (is (= '() (game/units-by-id-ids {}))))

  (testing "can update a unit"
    (let [state-1 {:game/units-by-id {}
                   :game/frame 0}
          state-2 {:game/units-by-id {1 {:unit/id 1}}
                   :game/frame 0}]
      (is (= (game/update-unit state-1 {:unit/id 1})
             state-2))))

  (testing "can update a list of units"
    (let [state-1 {:game/units-by-id {}
                   :game/frame 0}
          state-2 {:game/units-by-id {1 {:unit/id 1}
                                      2 {:unit/id 2}}
                   :game/frame 0}]
      (is (= (game/update-units state-1 [{:unit/id 1} {:unit/id 2}])
             state-2))
      (is (= (game/update-units state-1 [])
             state-1))))

  (testing "can identify a new unit"
    (is (true? (game/new-unit-id? {} 1)))
    (is (false? (game/new-unit-id? {:game/units-by-id {1 {}}} 1))))

  (testing "can find new unit ids from a list of unit ids"
    (let [state {:game/units-by-id {1 {}
                                    2 {}}}]
      (is (= (game/filter-new-units state [1 2 3])
             '(3)))))

  (testing "can map new units"
    (is (= (game/map-new-units {:game/frame 0} '(1))
           '({:unit/id 1
              :unit/frame-discovered 0
              :unit/last-frame-updated 0})))))