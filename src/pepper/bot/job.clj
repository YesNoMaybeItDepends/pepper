(ns pepper.bot.job
  (:refer-clojure :exclude [type new])
  (:require [pepper.api :as api])
  (:import
   [bwapi Game]))

(defn started-working? [[started? working?]]
  (and (not started?) working?))

(defn stopped-working? [[started? working?]]
  (and started? (not working?)))

(defn set-completed [job]
  (assoc job :completed? true))

(defn completed? [job]
  (:completed? job))

(defn set-run [job]
  (assoc job :run? true))

(defn run? [job]
  (:run? job))

(defn not-run? [job]
  ((complement run?) job))

(defn validate-job [job]
  (assert (:action job) "job requires an action")
  job)

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
                                (api/get-game api))))

(defn new [job]
  (if (map? job)
    (assoc job :uuid (random-uuid))
    {:uuid (random-uuid)}))

(defn execute-action! [job api]
  ((action job) api job))

(defn process-job! [job api]
  (cond
    (nil? job) nil
    (completed? job) nil
    :else (-> (execute-action! job api)
              (with-last-frame-executed! api))))

(comment "idea"
         {:im-a-job :blablaba
          :step :whatever-step
          :dispatcher-fn (fn dispatcher-fn [job game])})