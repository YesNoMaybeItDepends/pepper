(ns pepper.utils.portal
  (:require [portal.api :as p]))

(defn start! []
  (add-tap #'p/submit)
  (let [portal (p/open {:theme :portal.colors/nord})]
    portal))

(defn stop! [portal]
  (remove-tap #'p/submit)
  (p/close)
  (p/stop)
  nil)