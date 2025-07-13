(ns pepper.game.game-core
  (:require [pepper.game.query :as query]
            [pepper.api.game :as game]
            [pepper.interop :as api]
            [pepper.api.api :as bwapi]))

(defn query [])

(defn command [])

(defn main-with-jbwapi [client]
  (bwapi/api client))

(defn main-with-simapi [client]
  (bwapi/api client))

(defn main [store api] ;; or [store impl] ;; or [store handler <- already has game]
  #_())

(comment ;; init example with bwapi

  (let [client {}
        store {}
        api (bwapi/api client)]
    (main store api))

  #_())


(defn query-api
  [store api query]
  (swap! store query/send-request 4 query)
  (->> (api {:call/id (:id query)
             :call/input #_(:data query) []})
       (swap! store query/receive-response 4 query)))

;; ;; {:query/kind :query/game-units}

;; ;; {:query/kind :game/get-all-units}

;; ;; {:query/kind :query/unit
;; ;;  :query/data {:unit-id 12}}

;; ;; {:query/log
;; ;;  {{:query/kind :query/unit
;; ;;    :query/data {:unit-id 12}} [{:query/status :query.status/success
;; ;;                                 :query/frame 5
;; ;;                                 :query/result {:unit/id 12
;; ;;                                                :unit/type :unit.type/scv}}
;; ;;                                {:query/status :query.status/loading
;; ;;                                 :query/frame 4}]}}

;; ;; {:query/log
;; ;;  {{:query/kind :query/user
;; ;;    :query/data {:user-id "alice"}}

;; ;;   [{:query/status :query.status/success
;; ;;     :query/result {:user/id "alice"
;; ;;                    :user/given-name "Alice"
;; ;;                    :user/family-name "Johnson"
;; ;;                    :user/email "alice.johnson@acme-corp.com"}
;; ;;     :query/user-time #inst "2024-12-31T09:29:23.307-00:00"}

;; ;;    {:query/status :query.status/loading
;; ;;     :query/user-time #inst "2024-12-31T09:29:23.142-00:00"}]}}


;; ;; (:unit/type {:unit/type :unit.type/scv})



;; ;; #_(defn jbwapi-query-handler [[obj query args]]
;; ;;     (case obj
;; ;;       :game (game/query->method query)))

;; ;; #_(defn query->method [{:query/keys [kind data]}])

;; ;; #_(defn query->jbwapi [{:query/keys [kind data]}]
;; ;;     (case kind
;; ;;       :query/game-units
;; ;;       [:game :get-all-units]))


;; #_(cond-> {:query/status :query.status/error
;;            :query/frame frame}
;;     (:success? response) (assoc :query/status :query.status/success
;;                                 :query/result (:result response)))


;; (defn execute-api-query [api query]
;;   (api query)
;;   {:query/status :query.status/success
;;    :query/result {}})