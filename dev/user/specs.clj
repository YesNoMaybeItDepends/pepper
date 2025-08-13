(ns user.specs
  (:require
   [clojure.spec.test.alpha :as st]))

(defn instrument []
  (tap> {:spec-instrumented-fns (st/instrument)}))