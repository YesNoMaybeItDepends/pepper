(defproject pepper "0.1.0-SNAPSHOT"
  :repositories [["jitpack" "https://jitpack.io"]]
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [com.github.JavaBWAPI/JBWAPI "2.1.0"]]
  :main pepper.main
  :jvm-opts ["-Dclojure.compiler.elide-meta=[:doc :file :line :added]"
             "-Dclojure.compiler.direct-linking=true"
             #_"-Dclojure.compiler.disable-locals-clearing=true"]
  :global-vars {*warn-on-reflection* true}
  :aot :all)