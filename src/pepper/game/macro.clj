(ns pepper.game.macro
  (:require [pepper.game.unit :as unit]
            [pepper.game.player :as players]
            [pepper.game.jobs :as jobs]
            [pepper.game.player :as player]
            [pepper.game.resources :as resources]
            [clojure.string :as s])
  (:import (bwapi Unit Game UnitType)))

;;;; split mining out to harvesting or mining or something
;;;; split this into multiple files haha

(defn init-macro [state]
  {:workers #{}
   :mineral-fields #{}
   :geysers #{}
   :resource-depots #{}
   :supply-depots #{}
   :macro-orders #{}})

(defn get-workers [state]
  (->> (vals (:units-by-id state))
       (filter #(unit/ours? state %))
       (filter #(unit/worker? %))))

(defn get-command-centers [state]
  (->> (unit/get-units state)
       (filter #(unit/ours? state %))
       (filter #(unit/command-center? %))))

(defn get-idle-workers [state]
  (->> (get-workers state)
       (filter :idle?)))

(defn get-mineral-fields [state]
  (->> (unit/get-units state)
       (filter unit/mineral-field?)))

(defn assign-random-mineral
  [mineral-fields]
  (fn [worker]
    [worker (rand-nth mineral-fields)]))

(defn go-mine! [game job]
  (let [worker (Game/.getUnit game (:unit-id job))
        mineral-field (Game/.getUnit game (:mineral-field-id job))]
    (Unit/.gather worker mineral-field)))

(defn mining-job [[worker-id mineral-field-id]]
  {:job :mining
   ;;  :steps {:go-mine :mining
   ;;          :mining :done}
   :action go-mine!
   :unit-id worker-id
   :mineral-field-id mineral-field-id})

(defn process-idle-workers [state]
  (let [idle-workers (map :id (get-idle-workers state))
        mineral-fields (map :id (get-mineral-fields state))
        jobs-to-update (->> (map (assign-random-mineral mineral-fields)
                                 idle-workers)
                            (map mining-job))]
    (reduce jobs/assign-unit-job state jobs-to-update)))

(defn pending-request? [request]
  (not (:started? request)))

(defn request->requested [request]
  (:requested request))

(defn request->cost-tuple [request]
  (-> request
      request->requested
      resources/unit-type->cost))

(defn requests->total-cost [requests]
  (->> (map request->cost-tuple requests)
       (reduce resources/sum-quantities [0 0 0])))

(defn train-scv-request [unit-id]
  {:unit-id unit-id
   :requested UnitType/Terran_SCV
   :started? false})

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

(defn get-total-cost-of-pending-requests [state]
  (->> (get-requests state)
       (filter pending-request?)
       (requests->total-cost)))

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

(defn can-afford? [state unit-type]
  (let [need (resources/unit-type->cost unit-type)
        have (-> state
                 resources/get-state-resources
                 resources/resources->resource-tuple)
        reserved (get-total-cost-of-pending-requests state)]
    ;; TODO: I forgot to do (- have reserved) to get the real amounts
    (every? true? (map <= need have))))

(defn add-train-scv-job [state uid]
  (add-production-request state (train-scv-request uid)))

(defn process-train-workers [state]
  (let [command-centers (get-command-centers state)]
    (reduce (fn [state command-center]
              (if (and (not (already-requested? state UnitType/Terran_SCV))
                       (can-afford? state UnitType/Terran_SCV))
                (add-train-scv-job state (unit/id command-center))
                state))
            state
            command-centers)))

(defn process-production! [state game]
  state)

(defn process-macro [state]
  (-> state
      resources/process-resources
      process-idle-workers
      process-train-workers))
