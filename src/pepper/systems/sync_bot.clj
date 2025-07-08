(ns pepper.systems.sync-bot
  (:require
   [pepper.api.client :as client]
   [pepper.utils.chaoslauncher :as chaoslauncher]))

(defn bot-hooks
  "TODO: not used for anything right now"
  []
  {:on-init {:before []
             :after []}
   :on-start {:before []
              :after []}
   :on-frame {:before []
              :after []}})

(defn interceptor? [{:keys [fn args]}]
  (and (fn? fn) (or (vector? args)
                    (nil? args))))

(defn hook-interceptors [hooks interceptors]
  (reduce (fn [interceptors interceptor]
            (update-in interceptors
                       (:hook interceptor)
                       conj interceptor))
          hooks
          interceptors))

(defn parse-args [state args]
  (condp apply [args]
    vector? (mapv (fn [arg]
                    (if (and (vector? arg)
                             (every? keyword? arg))
                      (get-in state arg)
                      arg))
                  args)
    keyword? (case args
               :state [state]
               [])
    []))

(defn dispatch-interceptors! [state interceptors hook]
  (when-some [interceptors (get-in interceptors hook)]
    (when (every? interceptor? interceptors)
      (doseq [{:keys [fn args]} interceptors]
        (apply fn (into [] (parse-args state args)))))))

(defn bot [store]
  (swap! store
         (fn [state]
           (assoc state
                  :interceptors (hook-interceptors (bot-hooks) (:interceptors state)))))
  (fn [input]
    (let [state @store
          interceptors (:interceptors state)
          api (:api state)
          event (:event input)
          state (case event
                  :on-start (let [_ (dispatch-interceptors! state interceptors [:api :on-start :before])
                                  state (assoc state :game (.getGame api))
                                  _ (dispatch-interceptors! state interceptors [:api :on-start :after])]
                              state)
                  :on-frame (if-not (:game state)
                              state
                              (let [_ (dispatch-interceptors! state interceptors [:api :on-frame :before])
                                    state (update state :on-frame (fnil inc 0))
                                    _ (dispatch-interceptors! state interceptors [:api :on-frame :after])]
                                state))
                  :on-end (let [_ (println "ITS OVER")
                                state (assoc state :on-end true)]
                            state)
                  state)]
      (swap! store merge state)
      (when (= :on-end event)
        (shutdown-agents)))))

(defn- main [store]
  (let [bot (bot store)
        api (client/make-client bot)]
    (swap! store assoc :api api)
    (chaoslauncher/start!)
    (client/start-game api {:async true
                            :debug-connection false
                            :log-verbosely false})
    (println "done")
    (chaoslauncher/stop!)
    api))
  