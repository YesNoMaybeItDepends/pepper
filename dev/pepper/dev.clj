(ns pepper.dev
  (:require
   [pepper.core :as pepper]
   [pepper.systems.logging :as logging]
   [pepper.repl :as repl]))

(def initial-app-state false)
(def initial-store-state {:repl/ref nil #_(atom nil) ;; not used right now 
                          :repl/port 7888
                          :api/event-whitelist #{:on-start :on-frame :on-end}
                          :api/client nil
                          :api/in-chan nil
                          :api/out-chan nil
                          :api/err-chan nil})

(defonce app (atom initial-app-state))
(defonce store (atom initial-store-state))

(def interceptors
  "TODO: group by hook?"
  [#_{:hook [:api :on-start :before]
      :fn println
      :args ["[:api :on-start :before] interceptor"]}

   {:hook [:api :on-start :before]
    :fn (fn [api] (.pauseGame (.getGame api)))
    :args [[:api]]}

   #_{:hook [:api :on-start :after]
      :fn repl/start-server!
      :args [[:repl/ref] [:repl/port]]}

   #_{:hook [:api :on-frame :before]
      :fn println
      :args ["[:api :on-frame :before] interceptor"]}

   {:hook [:api :on-frame :after]
    :fn (fn [& args]
          (when-some [f (repl/dequeue!)]
            (apply f @store)))
    :args []}])

(defn main [& opts]
  ;; TODO: init portal ?
  (let [_ (logging/init-logging)]
    (reset! store initial-store-state)
    (swap! store assoc
           :interceptors interceptors)
    (reset! app (future (pepper/main store)))))

(defn reset []
  (reset! store initial-store-state)
  (reset! app nil))

(comment

  (main)
  (reset)

  #_())
