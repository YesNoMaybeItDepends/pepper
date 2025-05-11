(ns test.pepper.utils.interop-test
  (:require [clojure.test :refer [deftest testing are]]
            [pepper.utils.interop :as interop]))

(deftest test-interop
  (testing "interop"
    (testing "property->keyword"
      (are [from to] (= to (-> (#'pepper.utils.interop/property->keyword from)
                               :keyword))
        :a :a
        :FPS :fps
        :maxFPS :max-fps
        :maxFPSUnlimited :max-fps-unlimited
        :battleNet :battle-net))))