(ns pepper.utils.config
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest testing is]]))

(defn- read-edn [file]
  (edn/read-string (slurp file)))

(defn read-config [] (read-edn "config.local.edn"))
(def ^:private schema (read-edn "config.edn"))

(def config (read-config))

;;;; TODO: validate configs

(deftest test-config
  (println "\n\n!! TODO: validate configs !!\n\n\n")
  (testing "config"
    (is (= "valid" "valid"))))