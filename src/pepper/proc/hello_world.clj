(ns pepper.proc.hello-world
  (:require
   [pepper.proc.proc :as-alias p]))

(defn handle-hello
  [message]
  (assoc message ::p/message-type ::who))

(defn handle-who
  [message]
  (assoc message ::p/message-type ::world))

(defn handle-world
  [message]
  (assoc message ::p/message-type ::helo-world))

(defn handle-message [message]
  (case (::p/message-type message)
    ::hello (handle-hello message)
    ::who (handle-who message)
    ::world (handle-world message)))

(def xform (map handle-message))

(def proc {:proc-type ::hello-world
           :messages {::hello ::who
                      ::who ::world
                      ::world ::hello-world}
           :xform xform})