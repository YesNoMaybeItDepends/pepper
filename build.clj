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

(defn- jar-opts [opts]
  (let [app "pepper"
        version "0.0.1"
        target "target"
        classes (str target "/classes")]
    (assoc opts
           :app app
           :main 'pepper.main
           :version version
           :uber-file (str target "/" app ".jar")
           :manifest {"Add-Opens" "java.base/java.nio"} ;; do I still need this?
           :scm {:tag (str "v" version)}
           :basis (b/create-basis {})
           :class-dir classes
           :target-dir classes ;; for b/copy-dir
           :target target
           :path target ;; for b/delete
           :src-dirs ["src"])))

(defn uberjar [opts]
  (time (let [opts (jar-opts opts)]
          (println (with-time "Using JVM version" (jvm-version)))

          (println (with-time "Cleaning target dir..."))
          (b/delete opts)

          (println (with-time "Copying source to target dir..."))
          (b/copy-dir (update opts :src-dirs conj "resources"))

          (println (with-time "Compiling source..."))
          (b/compile-clj opts)

          (println (with-time "Building uberjar" (:uber-file opts) "..."))
          (b/uber opts)

          (println (with-time "Done")))))