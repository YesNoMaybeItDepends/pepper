(ns pepper.api
  (:require
   [clojure.core.async :as a]
   [pepper.api.bwem :as bwem]
   [pepper.api.client :as client])
  (:import
   [bwapi BWClient]))

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

;;;;

(defn get-client [api]
  (:api/client api))

(defn get-game [api]
  (:api/game api))

(defn get-bwem [api]
  (:api/bwem api))

(defn update-on-start [api]
  (let [game (BWClient/.getGame (get-client api))
        bwem (bwem/init! game)]
    (-> api
        (set-game game)
        (set-bwem bwem))))

(defn start-game! [api]
  (let [before-start (get-before-start api)
        client (get-client api)
        client-config (get-client-config api)
        after-end (get-after-end api)]

    (when (fn? before-start) (before-start))
    (client/start-game! client client-config)
    (when (fn? after-end) (after-end))))

(defn init [out-ch in-ch client-config before-start after-end]
  {:api/client (client/make-client
                (fn [event]
                  (a/put! out-ch event)))
   :api/out-ch out-ch
   :api/in-ch in-ch
   :api/client-config client-config
   :api/before-start before-start
   :api/after-end after-end})