(ns pepper.api
  (:require
   [clojure.core.async :as a]
   [pepper.api.bwem :as bwem]
   [pepper.api.client :as client])
  (:import
   [bwapi BWClient PerformanceMetrics PerformanceMetric]
   [bwem BWEM]))

;; If these are private, do I want these fns?

(defn ^:private get-client-config [api]
  (:api/client-config api))

(defn ^:private get-before-start [api]
  (:api/before-start api))

(defn ^:private get-after-end [api]
  (:api/after-end api))

(defn ^:private get-in-ch [api]
  (:api/in-ch api))

(defn ^:private get-out-ch [api]
  (:api/out-ch api))

(defn ^:private set-game [api game]
  (assoc api :api/game game))

(defn ^:private set-bwem [api bwem]
  (assoc api :api/bwem bwem))

(defn ^:private set-performance-metrics [api performance-metrics]
  (assoc api :api/performance-metrics performance-metrics))

;;;;

(defn client [api]
  (:api/client api))

(defn game [api]
  (:api/game api))

(defn bwem [api]
  (:api/bwem api))

(defn bwem-map [api]
  (:api/bwmap api))

(defn performance-metrics [api]
  (:api/performance-metrics api))

(defn update-on-start [api]
  (let [client (client api)
        game (BWClient/.getGame client)
        bwem (bwem/init! game)
        performance-metrics (BWClient/.getPerformanceMetrics client)]
    (-> api
        (set-game game)
        (set-bwem bwem)
        (assoc :api/bwmap (BWEM/.getMap bwem))
        (set-performance-metrics performance-metrics))))

(defn start-game! [api]
  (let [before-start (get-before-start api)
        client (client api)
        client-config (get-client-config api)
        after-end (get-after-end api)]

    (when (fn? before-start) (before-start))
    (client/start-game! client client-config)
    (when (fn? after-end) (after-end))))

(defn callback-async [out-ch]
  (fn [event]
    (a/put! out-ch event)))

(defn callback-blocking [out-ch in-ch]
  (fn [event]
    (a/>!! out-ch event)
    (a/<!! in-ch)))

(defn init [out-ch in-ch client-config before-start after-end]
  {:api/client (client/make-client (callback-blocking out-ch in-ch))
   :api/out-ch out-ch
   :api/in-ch in-ch
   :api/client-config client-config
   :api/before-start before-start
   :api/after-end after-end})

(defn datafy [obj keywords kw->val]
  (reduce (fn [acc kw]
            (assoc acc kw ((kw kw->val) obj)))
          {}
          keywords))

(def kw->running-total {:last #(.getLast %)
                        :max #(.getMax %)
                        :mean #(.getMean %)
                        :min #(.getMin %)
                        :samples #(.getSamples %)})

(defn datafy-running-total
  ([running-total] (datafy-running-total
                    running-total (keys kw->running-total)))

  ([running-total keywords]
   (datafy running-total keywords kw->running-total)))

(def kw->performance-metric {:interrupted PerformanceMetric/.getInterrupted
                             :running-total (fn [pm]
                                              (-> pm
                                                  PerformanceMetric/.getRunningTotal
                                                  datafy-running-total))
                             :string PerformanceMetric/.toString})

(defn datafy-performance-metric
  ([performance-metric] (datafy-performance-metric
                         performance-metric (keys kw->performance-metric)))

  ([performance-metric keywords]
   (datafy performance-metric keywords kw->performance-metric)))

(def kw->performance-metrics
  {:bot-idle PerformanceMetrics/.getBotIdle
   :bot-response PerformanceMetrics/.getBotResponse
   :client-idle PerformanceMetrics/.getClientIdle
   :communication-listen-to-receive PerformanceMetrics/.getCommunicationListenToReceive
   :communication-send-to-receive PerformanceMetrics/.getCommunicationSendToReceive
   :communication-send-to-sent PerformanceMetrics/.getCommunicationSendToSent
   :copying-to-buffer PerformanceMetrics/.getCopyingToBuffer
   :excess-sleep PerformanceMetrics/.getExcessSleep
   :flush-side-effects PerformanceMetrics/.getFlushSideEffects
   :frame-buffer-size PerformanceMetrics/.getFrameBufferSize
   :frame-duration-receive-to-receive PerformanceMetrics/.getFrameDurationReceiveToReceive
   :frame-duration-receive-to-send PerformanceMetrics/.getFrameDurationReceiveToSend
   :frame-duration-receive-to-sent PerformanceMetrics/.getFrameDurationReceiveToSent
   :frames-behind PerformanceMetrics/.getFramesBehind
   :intentionally-blocking PerformanceMetrics/.getIntentionallyBlocking
   :number-of-events PerformanceMetrics/.getNumberOfEvents
   :number-of-events-times-duration-receive-to-sent PerformanceMetrics/.getNumberOfEventsTimesDurationReceiveToSent})

(defn comp-datafy-performance-metric [m]
  (update-vals m #(comp datafy-performance-metric %)))

(defn datafy-performance-metrics
  ([performance-metrics]
   (datafy-performance-metrics
    performance-metrics (keys kw->performance-metrics)))

  ([performance-metrics keywords]
   (datafy performance-metrics keywords (comp-datafy-performance-metric kw->performance-metrics))))

(defn metrics [api]
  (-> performance-metrics
      datafy-performance-metrics))