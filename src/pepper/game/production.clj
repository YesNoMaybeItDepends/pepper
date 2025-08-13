(ns pepper.game.production
  "does nothing right now haha")

(defn pending-request? [request]
  (not (:started? request)))

(defn request->requested [request]
  (:requested request))

;; (defn request->cost-tuple [request]
;;   (-> request
;;       request->requested
;;       resources/unit-type->cost))

;; (defn requests->total-cost [requests]
;;   (->> (map request->cost-tuple requests)
;;        (reduce resources/sum-quantities [0 0 0])))

;; (defn train-scv-request [unit-id]
;;   {:unit-id unit-id
;;    :requested UnitType/Terran_SCV
;;    :started? false})

(defn already-requested? [state x]
  (let [c (get-in state [:production :table x])]
    (and (some? c)
         (>= c 1))))

(defn init-production [state]
  (assoc state :production {:queue clojure.lang.PersistentQueue/EMPTY
                            :table {}}))

(defn get-requests [state]
  (into [] (get-in state [:production :queue])))

(defn peek-production-request [state]
  (peek (get-in state [:production :queue])))

(defn add-production-request [state request]
  (update state :production
          (fn [p r]
            (-> p
                (update :queue conj r)
                (update-in [:table (:requested r)] (fnil inc 0))))
          request))

;; (defn get-total-cost-of-pending-requests [state]
;;   (->> (get-requests state)
;;        (filter pending-request?)
;;        (requests->total-cost)))

(defn take-production-request
  "state -> [state request]"
  [state]
  (let [request (peek (get-in state [:production :queue]))]
    [(update state :production
             (fn [p r]
               (-> p
                   (update :queue pop)
                   (update-in [:table (:requested r)] (fnil dec 1))))
             request)
     request]))

;; (defn add-train-scv-request [state uid]
;;   (add-production-request state (train-scv-request uid)))

;; (defn process-train-workers [state]
;;   (let [command-centers (get-command-centers state)]
;;     (reduce (fn [state command-center]
;;               (if (and (not (already-requested? state UnitType/Terran_SCV))
;;                        (can-afford-unit? state UnitType/Terran_SCV))
;;                 (add-train-scv-request state (unit/id command-center))
;;                 state))
;;             state
;;             command-centers)))
