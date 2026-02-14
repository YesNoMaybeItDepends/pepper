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

(defn version []
  (let [revs-count (b/git-count-revs nil)
        branch (clojure.tools.build.api/git-process
                {:git-args "branch --show-current"})]
    (format "0.1.%s" revs-count)))

(defn uberjar [opts]
  (time (let [build-folder "target"
              jar-content (str build-folder "/classes")
              basis (b/create-basis)
              app "pepper"
              file-name (str build-folder "/" app ".jar")]

          (println (with-time "Using JVM version" (jvm-version)))

          (println (with-time "Cleaning target dir..."))
          (b/delete {:path "target"})

          (println (with-time "Copying files to target dir..."))
          (b/copy-dir {:src-dirs   ["src" "resources"]
                       :target-dir jar-content})

          (println (with-time "Compiling source..."))
          (b/compile-clj {:basis     basis
                          :src-dirs  ["src"]
                          :compile-opts {;; :disable-locals-clearing false ?
                                         :elide-meta [:doc :file :line :added]
                                         :direct-linking true}
                          :bindings {} ;; {#'clojure.core/*assert* false #'clojure.core/*warn-on-reflection* true}
                          :class-dir jar-content})

          (println (with-time "Building uberjar" file-name "..."))
          (b/uber {:class-dir jar-content
                   :uber-file file-name
                   :basis     basis
                   :main      'pepper.main})

          (println "Done"))))