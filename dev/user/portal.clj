(ns user.portal
  (:require
   [portal.api :as portal]))

(def tapping (atom (with-meta {} {:portal.viewer/default
                                  :portal.viewer/tree})))
(def portal-ref (atom nil))

(defn init []
  (reset! portal-ref (portal/inspect tapping ;; did vscode not work?
                                     {:theme :portal.colors/nord
                                      :portal.viewer/default :portal.viewer/tree})))

(defn tap-job [job]
  (swap! tapping assoc-in [:jobs (:unit-id job)] job))

(defn tap-jobs [jobs]
  (let [jobs-by-unit-id (reduce (fn [unit-jobs unit-job]
                                  (assoc unit-jobs (:unit-id unit-job) unit-job))
                                {}
                                jobs)]
    (swap! tapping assoc :jobs jobs-by-unit-id)))

(defn tap-unit [unit]
  (swap! tapping assoc-in [:units (:id unit)] unit))

(defn tap-units [units])