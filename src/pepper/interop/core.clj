(ns pepper.interop.core
  (:require [clojure.spec.alpha :as s]))

;;;; request

(s/def :call/id (s/coll-of keyword? :kind vector?))
(s/def :call/input vector?)
(s/def :api/request (s/keys :req [:call/id
                                  :call/input]))
(s/def :request/args (s/cat :id :call/id
                            :input :call/input))
(s/fdef request
  :args :request/args)

(defn request [id input]
  {:call/id id
   :call/input input})

;;;; response

(s/def :call/status #{:processing :success :error})
(s/def :call/output any?)
(s/def :api/response (s/keys :req [:call/id
                                   :call/input
                                   :call/status
                                   :call/output]))
(s/def :response/args (s/cat :request :api/request
                             :status :call/status
                             :output :call/output))
(s/fdef response
  :args :response/args)

(defn response [{:call/keys [id input]} status output]
  {:call/id id
   :call/input input
   :call/status status
   :call/output output})

;;;; handler

(def help:handlers
  "A handler is a function that takes a request and returns a response
   
   - input
     - request
   - output
     - response"

  "See docstring")

;;;; middleware

(def help:middleware
  "See https://github.com/ring-clojure/ring/wiki/Concepts#middleware
   
   ```
   (fn wrap-foo [handler & args]
     (fn [request]
       (let [;; it can modify the request
             request-foo (foo-request request)
             ;; it can also modify the response
             response-foo (foo-response (handler request))
             response (fn [x] x)]
         response)))
   ```"

  "See docstring")

(defn wrap-middleware
  "TODO: does it work when the list of middleware fns is empty?"
  [handler middleware]
  #_(reduce (fn [handler middleware]
              (middleware handler))
            handler (into [] (flatten middleware)))
  ((apply comp (reverse (into [] (flatten middleware)))) handler))

;;;; router

(def help:router
  "A router is a function that takes routes as input and returns a function.
  The returned function takes a request as input and returns a map used to handle the given request as defined by the routes.
  
   - input
     - routes
   - output
     - fn
       - input
         - request
       - output
         - map with
           - handler fn
           - middleware array"

  "See docstring")

(defn router [routes]
  (fn [{:call/keys [id]
        :as request}]
    (get-in routes id)))

;;;; api

(def help:ring-handler
  "Ring works like this:
   
   - `((ring-handler ring-router) request)` -> response

     - `ring-handler`, given a ring-router, returns a handler

     - `ring-router`, given a request, returns a handler

     - `handler`, given a request, returns a response"

  "See docstring")

(defn api-handler
  "TODO: add middleware
   TODO: handle empty middleware"
  ([router]

   (fn [request]
     (let [{:keys [handler
                   middleware]} (router request)
           #_handler #_(wrap-middleware handler middleware)]
       (handler request)))))