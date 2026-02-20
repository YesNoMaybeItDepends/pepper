(defproject pepper "0.1.0-SNAPSHOT"
  :repositories [["jitpack" "https://jitpack.io"]]
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [com.github.JavaBWAPI/JBWAPI "2.1.0"]]
  :source-paths      ["src/clj"]
  :java-source-paths ["src/java"]
  :main pepper.Bot
  :jvm-opts ["-Dclojure.compiler.elide-meta=[:doc :file :line :added]"
             "-Dclojure.compiler.direct-linking=true"
             #_"-Dclojure.compiler.disable-locals-clearing=true"]
  :repl-options {:init-ns pepper.main}
  :global-vars {*warn-on-reflection* true}
  :javac-options     [#_"-target" #_"8"
                      #_"-source" #_"8"
                      "--release" "8"]
  :profiles {:uberjar {:aot :all}})