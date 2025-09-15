(ns pepper.bot.job
  (:refer-clojure :exclude [type new])
  (:require
   [pepper.api :as api]
   [pepper.bot.job :as job])
  (:import
   [bwapi Game Unit]))

(def xforms (atom {}))

(defn register-xform! [xform-id xform]
  (swap! xforms assoc xform-id xform))

;;;;

(defn started-working? [[started? working?]]
  (and (not started?) working?))

(defn stopped-working? [[started? working?]]
  (and started? (not working?)))

(defn is-working? [[started? working?]]
  (and started? working?))

(defn set-completed [job]
  (assoc job :job/completed? true))

(defn completed? [job]
  (:job/completed? job))

(defn set-cancelled [job]
  (assoc job :job/cancelled? true))

(defn cancelled? [job]
  (:job/cancelled? job))

(defn set-cost [job cost]
  (assoc job :cost cost))

(defn cost [job]
  (:cost job))

(defn set-cost-paid [job frame]
  (assoc job :frame-cost-paid frame))

(defn cost-paid? [job]
  (:frame-cost-paid job))

(defn validate-job [job]
  (assert (:action job) "job requires an action")
  job)

(defn job [job]
  (:job job))

(defn job? [m]
  (some? (job m)))

(defn type [job]
  (:job job))

(defn type? [job-types]
  (let [types (if (set? job-types) job-types
                  (into #{} (flatten [job-types])))]
    (fn [job]
      (types (type job)))))

(defn unit-id [job]
  (:unit-id job))

(defn set-action [job action]
  (assoc job :action action))

(defn action [job]
  (:action job))

(defn step [job]
  (:step job))

(defn set-step [job step]
  (assoc job :step step))

(defn set-last-frame-executed [job frame]
  (assoc job :frame-last-executed frame))

(defn tick [job]
  (:tick job))

(defn with-tick-inc [job]
  (update job :tick (fnil inc 0)))

(defn with-last-frame-executed! [job api] ;; disgusting
  (set-last-frame-executed job (Game/.getFrameCount
                                (api/game api))))

(defn init [job frame]
  ((fnil merge {}) job {:uuid (random-uuid)
                        :frame-created-job frame}))

(defn xform-id [job]
  (:xform-id job))

(defn xform!
  "executes job on xform"
  [job api]
  (if-some [xform ((xform-id job) @xforms)]
    (xform [job api])
    job))

(defn xform? [job]
  (some? (xform-id job)))

(defn execute-action!
  "executes job on action"
  [job api]
  ((action job) api job))

(defn process-job! [job api]
  (cond
    (nil? job) nil
    (completed? job) nil
    (cancelled? job) nil
    (and (nil? (xform-id job)) (nil? (action job))) nil
    :else (-> (if (xform? job)
                (xform! job api)
                (execute-action! job api))
              (with-last-frame-executed! api)
              (with-tick-inc))))

(defn debug-job! [job api]
  (let [game (api/game api)
        unit (Game/.getUnit (unit-id job))]
    (merge job {:debug-unit {:exists? (Unit/.exists unit)
                             :completed? (Unit/.isCompleted unit)
                             :idle? (Unit/.isIdle unit)
                             :order (Unit/.getOrder unit)
                             :order-target (Unit/.getOrderTarget unit)
                             :order-target-position (Unit/.getOrderTargetPosition unit)
                             :order-timer (Unit/.getOrderTimer unit)
                             :last-command-frame (Unit/.getLastCommandFrame unit)
                             :last-command (Unit/.getLastCommand unit)
                             :can-command (Unit/.canCommand unit)
                             :can-gather (Unit/.canGather unit)}})))