(ns pepper.utils.profiling)

;; config
;; target-ms -> 20 or 25?
;; limit-ms -> 30 or 35?

(defn now-ns []
  (System/nanoTime))

(defn ns->ms [nanoseconds]
  (java.util.concurrent.TimeUnit/.convert
   java.util.concurrent.TimeUnit/MILLISECONDS
   nanoseconds
   java.util.concurrent.TimeUnit/NANOSECONDS))

(defn duration [start end]
  (- end start))

(defn times->duration [times]
  (let [sorted (sort times)
        start (first sorted)
        end (last sorted)]
    (- end start)))

(defn times-ns->duration-ms [times-ns]
  (ns->ms (times->duration times-ns)))

(defn within-margin-min-max [duration-ms target-ms margin-ms]
  (let [min (- target-ms margin-ms)
        max (+ target-ms margin-ms)]
    (when (and (>= duration-ms min)
               (<= duration-ms max))
      true)))

(defn within-limit-ms [duration-ms target-ms limit-ms]
  (when (and (<= duration-ms (+ target-ms limit-ms))
             (>= duration-ms (- target-ms limit-ms)))
    true))