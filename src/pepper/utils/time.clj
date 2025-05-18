(ns pepper.utils.time)

(defn timestamp []
  (java.time.Instant/now))