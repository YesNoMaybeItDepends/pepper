(ns user.portal
  (:require [portal.api :as p]))

(defonce viewer
  {:portal.viewer/default :portal.viewer/table
   :portal.viewer/table {:columns [:id :value :time]}})

(defonce tap-list (atom (with-meta [] viewer)))

(defonce ids (atom 0))

(defn submit [value]
  (let [id (swap! ids inc)]
    (swap! tap-list
           (fn [taps]
             (conj
              (if (< (count taps) 5)
                taps
                (subvec taps 1))
              {:id    id
               :value value
               :time  (java.util.Date.)})))))

(defn start-portal! []
  (add-tap #'submit)
  (let [portal (p/open {:value tap-list
                        :theme :portal.colors/nord})]
    portal))

(defn stop-portal! [#_portal]
  (remove-tap #'p/submit)
  (p/close)
  (p/stop)
  nil)