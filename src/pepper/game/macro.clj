(ns pepper.game.macro
  (:require [pepper.game.unit :as unit]
            [pepper.game.player :as players]
            [pepper.game.jobs :as jobs]
            [pepper.game.player :as player]
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

(defn cost [minerals gas supply]
  [minerals gas supply])

(defn unit-type->resource-tuple
  "Assumes unit-type is a java enum
   Returns a tuple of [minerals gas supply]
   TODO: time is also a factor which would nullify this idea"
  [unit-type]
  (if (instance? UnitType unit-type)
    [(unit/mineral-cost unit-type)
     (unit/gas-cost unit-type)
     (unit/supply-cost unit-type)]
    (throw (Exception. "I have to fix unit-type handling lol"))))

(defn get-frame-resources [state]
  (let [self (players/get-self state)]
    (-> {}
        (assoc :minerals-total (players/minerals self))
        (assoc :gas-total (players/gas self))
        (assoc :supply-total (players/supply-total self))
        (assoc :supply-used (players/supply-total self)))))

(defn init-resources
  "TODO: resources could be the AVAILABLE [minerals gas supply]
   for supply, that would be total - used"
  [state]
  (assoc state :resources {:gas 0
                           :minerals 0
                           :supply [0 0]}))

(defn state->resources [state]
  (:resources state))

(defn resources->minerals [resources]
  (:minerals resources))

(defn resources->gas [resources]
  (:gas resources))

(defn resources->supply [resources]
  (:supply resources))

(defn supply->supply-used [[used _]]
  used)

(defn supply->supply-total [[_ total]]
  total)

(defn supply->supply-available [[used total]]
  (- total used))

(defn supply [[used total]]
  {:supply-used used
   :supply-total total})

(defn resources->resource-tuple [resources]
  [(resources->minerals resources)
   (resources->gas resources)
   (supply->supply-available (resources->supply resources))])

(defn get-our-minerals [state]
  (players/minerals (players/get-self state)))

(defn get-our-gas [state]
  (players/gas (players/get-self state)))

(defn get-our-supply [state]
  (let [p (players/get-self state)]
    [(players/supply-used p) (players/supply-total p)]))

(defn update-resources [state resources]
  (assoc state :resources resources))

(defn process-resources [state]
  (-> state
      (update-resources {:minerals (get-our-minerals state)
                         :gas (get-our-gas state)
                         :supply (get-our-supply state)})))

(defn pending-request? [request]
  (not (:started? request)))

(defn request->requested [request]
  (:requested request))

(defn request->cost-tuple [request]
  (-> request
      request->requested
      unit-type->resource-tuple))

(defn sum-resource-costs [a b]
  (mapv + a b))

(defn requests->total-cost [requests]
  (->> (map request->cost-tuple requests)
       (reduce sum-resource-costs [0 0 0])))

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
  (let [need (unit-type->resource-tuple unit-type)
        have (-> state
                 state->resources
                 resources->resource-tuple)
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
      process-resources
      process-idle-workers
      process-train-workers))
