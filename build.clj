(ns build
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.build.api :as b]))

(defn jvm-version []
  (-> (System/getProperty "java.specification.version")))

(defn HH-mm-ss []
  (let [formatter (java.time.format.DateTimeFormatter/ofPattern "HH:mm:ss")
        current-time (java.time.LocalTime/now)]
    (java.time.LocalTime/.format current-time formatter)))

(defn with-time [& str]
  (clojure.string/join " " (concat [(HH-mm-ss) "-"] str)))

(defn uberjar [opts]
  (time (let [build-folder "target"
              jar-content (str build-folder "/classes")
              basis (b/create-basis {:project "deps.edn"})
              app "pepper"
              file-name (str build-folder "/" app ".jar")]

          (println (with-time "Using JVM version" (jvm-version)))

          (println (with-time "Cleaning target dir..."))
          (b/delete {:path "target"})

          (println (with-time "Copying files to target dir..."))
          (b/copy-dir {:src-dirs   ["resources"]
                       :target-dir jar-content})

          (println (with-time "Compiling source..."))
          (b/compile-clj {:basis     basis
                          :src-dirs  ["src"]
                          :class-dir jar-content})

          (println (with-time "Building uberjar" file-name "..."))
          (b/uber {:class-dir jar-content
                   :uber-file file-name
                   :basis     basis
                   :main      'pepper.main})

          (println "Done"))))