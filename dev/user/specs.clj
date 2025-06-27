(ns user.specs
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as st]
            [pepper.htn.planner :as planner]
            [pepper.htn.impl.primitive :as primitive]
            [pepper.htn.impl.compound :as compound]
            [pepper.interop :as interop]))

(defn instrument []
  (tap> {:spec-instrumented-fns (st/instrument)}))