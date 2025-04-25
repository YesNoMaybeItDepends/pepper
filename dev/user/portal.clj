(ns user.portal
  (:require [portal.api :as p]))

(defn start-portal! []
  (let [portal (p/open {:theme :portal.colors/nord})]
    (add-tap #'p/submit)
    portal))

(defn stop-portal! [portal]
  (remove-tap #'p/submit)
  (p/close)
  (p/stop)
  nil)