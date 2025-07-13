(ns pepper.game.game-core-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.spec.test.alpha :as st]
   [clojure.spec.alpha :as s]
   [pepper.game.query :as query]))

(st/instrument)
(s/check-asserts true)