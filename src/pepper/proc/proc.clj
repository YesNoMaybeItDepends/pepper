(ns pepper.proc.proc
  (:require
   [clojure.core.async :as a :refer [chan sub mix admix pipe]]
   [pepper.proc.xforms :as xform]
   [clojure.spec.alpha :as s]
   [pepper.proc.queue :as q]))

;;;; Proc definition
(s/def ::proc-type keyword?)
(s/def ::messages (s/map-of keyword? keyword?))
(s/def ::xform fn?)
(s/def ::requires any?)
(s/def ::proc
  (s/keys
   :req [::proc-type ::messages ::xform]
   :opt [::requires]))

;;;; Message definition
(s/def ::message-type keyword?)
(s/def ::message-id any?)
(s/def ::request-id any?)
(s/def ::request-inst any?)
(s/def ::requester-id any?)
(s/def ::created-at any?)
(s/def ::updated-at any?)
(s/def ::context any?)
(s/def ::message
  (s/keys
   :req [::message-type]
   :opt [::message-id
         ::request-id
         ::request-inst
         ::requester-id
         ::created-at
         ::updated-at
         ::context
         ::message]))

(defn proc->inputs
  "Get the inputs of a Proc"
  [proc]
  (keys (:messages proc)))

(defn proc->outputs
  "Get the outputs of a Proc"
  [proc]
  (vals (:messages proc)))

(defn input->sub
  "Creates a channel subscribed to the input topic"
  [topic pub]
  (sub pub topic (chan)))

(comment ;; TODO

  "TODO: make my proc topic subscribing function return a map of :sub-fn -> sub
    ALTERNATIVELY, make the sub function find in the list of topics
    EXAMPLE now -> 1 :something 2 :something-else
    EXAMPLE idea -> 1 #{:something something-else} message-type 
     -> handles both in a single function, is based"
  (let [proc @(atom {})
        hello-world (:hello-world proc)
        subs (:subscriptions hello-world)]
    {:count (count subs)
     :topics (map type subs)})

  #_())


(defn proc->subs
  "Creates a list of subscriptions for a Proc"
  [proc pub]
  (map (fn [topic] (input->sub topic pub))
       (proc->inputs proc)))

(defn mix-channels
  "Mixes a list of channels into a single mix"
  [channels out]
  (let [m (mix out)]
    (doseq [channel channels]
      (admix m channel))
    m))

(s/fdef init
  :args (s/keys :req-un [::proc ::pub ::output]
                :opt-un [::xf-config])
  :ret (s/keys :req-un [::channel ::subscriptions ::mix ::pipe]))
(defn init
  "Turns proc definition into channel that should also probably output messages tbqh fam"
  [{:keys [proc pub output xf-config] :or {xf-config {}}}]
  (let [xf (xform/compose-xf proc)
        ch (chan 1 xf)
        subs (proc->subs proc pub)
        mix (mix-channels subs ch)
        pipe (pipe ch output)]
    {:channel ch
     :subscriptions subs
     :mix mix
     :pipe pipe
     #_[:xform #_#'xf]})) ;; TODO: if I leave the xform, printing this throws error

(defn next-message-type
  "unused for now because it requires passing proc as an argument, which could be tricky"
  [message proc]
  {::message-type (get (:messages proc) (::message-type message))})