(ns pepper.game.query
  (:require
   [pepper.api.game :as game]))

;; utils

(defn get-latest-status [state query]
  (:query/status (first (get-in state query))))

(defn loading? [state query]
  (= :query.status/loading
     (get-latest-status state [::log query])))

(defn available? [state query]
  (= :query.status/success
     (get-latest-status state [::log query])))

(defn add-log-entry [log entry]
  (cons entry log))

(defn get-result [state query]
  (->> (get-in state [::log query])
       (keep :query/result)
       first))

;; fns

(defn send-request [state frame query]
  (update-in state [::log query] add-log-entry
             {:query/status :query.status/loading
              :query/frame frame}))



(defn receive-response [state frame query response]
  (update-in state [::log query] add-log-entry
             (cond->
              {:query/frame frame}

               (:success? response)
               (assoc :query/status :query.status/success
                      :query/result (:result response))

               (false? (:success? response))
               (assoc :query/status :query.status/error))))

(defn execute-call [])

(defn dispatch-call [call])

#_{:request/path [:api :id]
   :request/data {}}


;; (defn parse-call-id
;;   "Input
;;    - `:game/get-all-units`

;;    Output
;;    - `[:game :get-all-units]`"
;;   [call]
;;   (mapv keyword [(namespace call)
;;                  (name call)]))

;; (defn parse-call [call]
;;   (let [[api id] (parse-call-id call)]
;;     (case api)))

;; (defn get-call [call get-call-fn]
;;   (let [[api id] (parse-call-id call)]
;;     (get-call-fn [api id])))