(ns pepper.utils.config
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest testing is]]))

(def dir {:ai ".\\bwapi-data\\AI"
          :read ".\\bwapi-data\\read"
          :write ".\\bwapi-data\\write"})

(defn- read-edn [file]
  (println "reading file " file)
  (let [file (edn/read-string
              (try (slurp file)
                   (catch Exception e
                     (println "Could not read file" file)
                     "{}")))]
    (println file)
    file))

(defn read-config []
  (read-edn (str (:ai dir) "\\config.local.edn")))

;;;; TODO: validate configs

(deftest test-config
  (println "\n\n!! TODO: validate configs !!\n\n\n")
  (testing "config"
    (is (= "valid" "valid"))))