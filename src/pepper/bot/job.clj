(ns pepper.bot.job
  (:refer-clojure :exclude [type new])
  (:require
   [pepper.api :as api])
  (:import
   [bwapi Game Unit]))

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

(defn set-run [job]
  (assoc job :run? true))

(defn run? [job]
  (:run? job))

(defn not-run? [job]
  ((complement run?) job))

(defn validate-job [job]
  (assert (:action job) "job requires an action")
  job)

(defn job? [job]
  (and (contains? job :job)
       (contains? job :action)
       (contains? job :unit-id)))

(defn type [job]
  (:job job))

(defn type? [job job-type]
  (= job-type (type job)))

(defn unit-id [job]
  (:unit-id job))

(defn set-action [job action]
  (assoc job :action action))

(defn action [job]
  (:action job))

(defn set-last-frame-executed [job frame]
  (assoc job :frame-last-executed frame))

(defn with-last-frame-executed! [job api] ;; disgusting
  (set-last-frame-executed job (Game/.getFrameCount
                                (api/game api))))

(defn init [job frame]
  ((fnil merge {}) job {:uuid (random-uuid)
                        :frame-created-job frame}))

(defn execute-action! [job api]
  ((action job) api job))

(defn process-job! [job api]
  (cond
    (nil? job) nil
    (completed? job) nil
    (cancelled? job) nil
    :else (-> (execute-action! job api)
              (with-last-frame-executed! api))))

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

(comment "idea"
         {:im-a-job :blablaba
          :step :whatever-step
          :dispatcher-fn (fn dispatcher-fn [job game])})