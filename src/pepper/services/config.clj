(ns pepper.services.config
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest testing is]]))

(defn- read-edn [file]
  (edn/read-string (slurp file)))

(defn get-config [] (read-edn "config.local.edn"))
(def ^:private schema (read-edn "config.edn"))

;;;; TODO: validate configs

(deftest test-config
  (println "\n\n!! TODO: validate configs !!\n\n\n")
  (testing "config"
    (is (= "valid" "valid"))))