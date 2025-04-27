(ns pepper.proc.xforms)

(def printing-tapping-xf
  (map (fn print-tap
         [x]
         (println x)
         (tap> x)
         x)))

(def logging-xf
  (map (fn print-message [message]
         (println message)
         message)))

(defn compose-xf-cooked
  "TODO: its cooked"
  ([xf] (comp xf))
  ([xf {:keys [before after]
        :or {before []
             after []}}]
   (println before)
   (println after)
   (println "flattened" (flatten [before xf after]))
   (apply comp (reverse (flatten [before xf after])))))

(defn compose-xf [{proc-xf :xform}]
  (comp
   (partial logging-xf)
   (partial proc-xf)))