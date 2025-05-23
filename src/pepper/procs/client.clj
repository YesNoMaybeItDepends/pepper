(ns pepper.procs.client
  (:require
   [clojure.core.async :as a]
   [clojure.core.async.flow :as flow]
   [clojure.spec.alpha :as s]
   [pepper.api.client :as c]
   [pepper.api.game :as g]))

(defn describe
  []
  {:params
   {:client-config "client config"
    :ch-from-client "messages from the client"
    :ch-to-client "messages to the client"}

   :ins
   {:with-game "anything to do with the game"}

   :outs
   {:out "blabla"}})

(defn init
  [{:keys [client-config
           ch-from-client
           ch-to-client] :as args}]
  (let [client (c/make-client {:to-client ch-to-client
                               :from-client ch-from-client})]
    (assoc args
           :client client
           :client-config client-config
           ::flow/in-ports {:from-client ch-from-client}
           ::flow/out-ports {:to-client ch-to-client})))

(defn transition
  [{:keys [client client-config] :as state} transition]
  (case transition
    ::flow/resume ;; none of these work how I need, because they're run on different threads / asynchronously, but the client api is asynchronous
    #_(do (c/start-game client (c/configuration client-config))
          state)
    #_(assoc state :go-game (a/go (c/start-game client (c/configuration client-config))))
    #_(do (future (c/start-game client (c/configuration client-config)))
          state)))

(defn transform
  [{:keys [client game] :as state} id msg]
  (case id
    :from-client (case (:event msg)
                   :on-start (do (println "from-client ->" (:event msg))
                                 (let [game (c/get-game client)]
                                   [(assoc state :game game) nil]))

                   :on-frame (do (println "from-client ->" (:event msg))
                                 #_(g/draw-text-screen game 100 100 (str (g/get-frame-count game)))
                                 [state nil])

                   (do (println "from-client ->" (:event msg))
                       [state nil]))))

(defn proc
  ([] (#'describe))
  ([args] (#'init args))
  ([state lifecycle] (#'transition state lifecycle))
  ([state id msg] (#'transform state id msg)))