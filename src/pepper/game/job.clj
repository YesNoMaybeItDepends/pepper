(ns pepper.game.job
  (:refer-clojure :exclude [type]))

(defn started-working? [[started? working?]]
  (and (not started?) working?))

(defn stopped-working? [[started? working?]]
  (and started? (not working?)))

(defn set-completed [job]
  (assoc job :completed? true))

(defn completed? [job]
  (:completed? job))

(defn validate-job [job]
  (assert (:action job) "job requires an action")
  job)

(defn type [job]
  (:job job))

(defn type? [job job-type]
  (= job-type (type job)))

(defn unit-id [job]
  (:unit-id job))

(defn set-last-frame-executed [job frame]
  (assoc job :frame-last-executed frame))

(comment "idea"
         {:im-a-job :blablaba
          :step :whatever-step
          :dispatcher-fn (fn dispatcher-fn [job game])})