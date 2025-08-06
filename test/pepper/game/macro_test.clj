(ns pepper.game.macro-test
  {:clj-kondo/config '{:lint-as {clojure.test.check.clojure-test/defspec clj-kondo.lint-as/def-catch-all}}}
  (:require
   [clojure.spec.test.alpha :as st]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.properties :as prop]
   [pepper.game.jobs :as jobs]
   [pepper.game.macro :as macro]
   [pepper.game.resources :as resources]
   [pepper.generators :as gen])
  (:import
   (bwapi UnitType)))

(st/instrument)

;; haha