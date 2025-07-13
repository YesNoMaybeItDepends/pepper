(ns pepper.api.api
  (:require
   [pepper.api.game :as game]
   [pepper.interop :as i]))

;; (defn get-handler [[api id]]
;;   (case api
;;     :game (game/id->call id)))

;; (defn get-handler [[api id]]
;;   (case api
;;     :game (game/id->call id)))

;; (defn router [request])

#_(defn router [client]
    (i/router (merge {} (game/routes client))))

#_(defn api [client]
    (i/root-handler (router)))

;;;; junk to organize

;;   #_{:method fn #_"method to invoke"
;;      :response fn #_"handles the response"}
;;   (defn id->call [id]
;;     (id {:get-all-units-v1 (partial get-all-units)
;;          :get-all-units-v2 (fn [game] (partial (.getAllUnits game)))
;;          :get-all-units-v3 {:method (partial get-all-units)
;;                             :response (fn [res]
;;                                         res)}}))

;;   (defn query->method [kind]
;;     (case kind
;;       #_:get-all-units (fn [game] (partial (.getAllUnits game)))
;;       :get-all-units (partial get-all-units)))

;;   (defn call [api method data]
;;     (method api))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;   seems alright   ;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;   {:call/id :game/is-explored ;;
;;    :call/data {:tile-x 0      ;;
;;                :tile-y 0}}    ;;
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;   {:api/call {:call/id :game/is-explored}
;;    :api/data {:tile-x 0
;;               :tile-y 0}}
;;   {:api/call :game/is-explored
;;    :api/data {:tile-x 0
;;               :tile-y 0}}
;;   {:api/call :game/get-all-units}
;;   [:api/game :game/get-all-units]
;;   #_[:api/game :get-all-units]
;;   #_[:game :get-all-units]
;;   #_[:game :game/get-all-units]

;;   {:methods #{:get-all-units}}
;;   {:game/methods #{:game/get-all-units}}

;;   #_{:call/status #{:success :error}}