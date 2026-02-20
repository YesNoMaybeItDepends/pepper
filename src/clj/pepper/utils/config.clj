(ns pepper.utils.config
  (:require [clojure.edn :as edn]))

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