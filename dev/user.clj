(ns user
  (:require
   [clojure.reflect :as reflect]
   [clojure.core.async :as a]
   [clojure.core.async.flow :as flow]
   [clojure.core.async.flow-monitor :as flow-monitor]
   [clojure.java.process :as process]
   [clojure.java.io :as io]
   [clojure.repl :as repl]
   [clojure.pprint :as pprint]
   [zprint.core :as zp]
   [clojure.spec.alpha :as s]
   [pepper.core :as pepper]
   [flow-storm.api :as fs-api]
   [taoensso.telemere :as t]
   #_[clojure.spec.test.alpha :as st]
   [portal.api :as portal]
   [clojure.tools.namespace.find :as ns-find]
   [clojure.java.classpath :as cp]
   [clojure.tools.namespace.parse :as ns-parse]
   [clojure.string :as str]
   [user.specs :as specs]
   [pepper.dev :as dev]))

;; Would be better if it was a function that returned the config or something like that
;; So that it would be changeable
(def system-config
  {:config/portal {:config/auto-init? true
                   :config/is-init? :portal/instance
                   :config/init-fn (fn [state]
                                     (add-tap #'portal/submit)
                                     (assoc state :portal/instance (portal/open {:theme :portal.colors/nord})))
                   :config/stop-fn (fn [state]
                                     (remove-tap #'portal/submit)
                                     (portal/close (:portal/instance state))
                                     (portal/stop)
                                     (assoc state :portal/instance nil))}
   :config/zprint {:config/auto-init? true
                   :config/is-init? :zprint/tapped
                   :config/init-fn (fn [state]
                                     (add-tap #'zp/zprint)
                                     (assoc state :zprint/tapped true))
                   :config/stop-fn (fn [state]
                                     (remove-tap #'zp/zprint)
                                     (assoc state :zprint/tapped false))}
   ;; Instrumentation isn't a system but I'll rewrite this at some other time
   :config/instrumentation {:config/auto-init? true
                            :config/is-init? :instrumentation/instrumented
                            :config/init-fn (fn [state]
                                              (specs/instrument)
                                              state)
                            :config/stop-fn (fn [state]
                                              state)}})

;; another config idea
#_{:config {:config/id :portal
            :config/should-init? (fn [state config] (not ((:config/id config) state)))
            :config/init-fn (fn [] :val)}
   :state {}
   :init-fn (fn [state some-config]
              (if ((:config/should-init? some-config) state some-config)
                (assoc state (:config/id some-config) (:config/init-fn some-config))
                state))}

(defn init-config! [state {:config/keys [auto-init?
                                         is-init?
                                         init-fn]}]
  (if (and (true? auto-init?)
           (ifn? is-init?)
           (not (is-init? state)))
    (init-fn state)
    state))

(defn stop-config! [state {:config/keys [is-init?
                                         stop-fn]}]
  (if (and (ifn? is-init?)
           (is-init? state))
    (stop-fn state)
    state))

(defn init-system [config]
  (let [config config
        store (atom {})]

    (fn
      ([] {:atom store
           :config config})
      ([action]
       (let [action-fn (case action
                         :init #'init-config!
                         :stop #'stop-config!
                         (fn [state _] state))
             state (reduce (fn [state [entry entry-config]]
                             (action-fn state entry-config))
                           @store
                           config)]
         (swap! store merge state))))))

(defonce system (init-system system-config))
(system :stop)
(system :init)

(comment

  (set! *print-namespace-maps* false)

  (try
    (dev/main)
    (catch Exception e (println e)))

  (try
    (dev/reset)
    (catch Exception e (println e)))

  @dev/store

  (tap> @dev/store) ;;;; won't tap the ingame bot state, just the "outer shell"

  (let [state @dev/store
        in-chan (:api/in-chan state)
        out-chan (:api/out-chan state)
        event {:event :tap}] ;; or {:event :hello-world}
    (a/>!! in-chan event)
    (a/<!! out-chan))

  (-> (:api/client @dev/store)
      .getGame
      .resumeGame)

  (-> (:api/game @dev/store)
      .pauseGame)

  (-> (:api/game @dev/store)
      .resumeGame)

  #_())
