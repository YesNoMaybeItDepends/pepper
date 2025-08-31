(ns user.portal
  (:require
   [portal.api :as portal]))

(def jobs-by-unit-id (atom (with-meta {} {:portal.viewer/default :portal.viewer/table})))

(defn tap-unit-job [unit-id]
  (doto (with-meta (get @jobs-by-unit-id unit-id [:nil])
          {:portal.viewer/default :portal.viewer/table})
    tap>
    println))

(defn without-last-frame-executed [job]
  (dissoc job :frame-last-executed))

(defn update-jobs [new-jobs]
  (swap! jobs-by-unit-id
         (fn [jobs-by-unit-id new-jobs]
           (reduce
            (fn [acc curr]
              (update-in acc [(:unit-id curr)]
                         (fn [jobs job]
                           (if (some #(= (without-last-frame-executed %)
                                         (without-last-frame-executed job))
                                     jobs)
                             jobs
                             (let [x (into (or jobs []) conj (flatten [job]))]
                               (if (< (count x) 11)
                                 x
                                 (subvec x 1))))) curr))
            jobs-by-unit-id
            new-jobs))
         new-jobs))