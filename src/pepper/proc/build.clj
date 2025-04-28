(ns pepper.proc.build
  (:require
   [pepper.proc.proc :as proc]
   [clojure.test :refer [deftest is testing]]))

(defn handle-empty
  [message]
  {::proc/message-type ::build-something})

(defn handle-build-something
  [message]
  (assoc message ::proc/message-type ::lock-budget))

(defn handle-lock-budget
  [message]
  (assoc message ::proc/message-type ::budget-locked))

(defn handle-budget-locked
  [message]
  (assoc message ::proc/message-type ::start-building))

(defn handle-start-building
  [message]
  (assoc message ::proc/message-type ::building-finished))

(defn handle-building-finished
  [message]
  (assoc message ::proc/message-type ::something-built))

(defn handle-message [message]
  (case (::proc/message-type message)
    ::build-something (handle-build-something message)
    ::lock-budget (handle-lock-budget message)
    ::budget-locked (handle-budget-locked message)
    ::start-building (handle-start-building message)
    ::building-finished (handle-building-finished message)
    (handle-empty message)))

(def xform (map handle-message))

(def proc
  {:proc-type ::build-proc
   :messages {::build-something ::lock-budget
              ::lock-budget ::budget-locked
              ::budget-locked ::start-building
              ::start-building ::building-finished
              ::building-finished ::something-built
              #_::budget-locked #_::find-place
              #_::place-found #_::find-scv
              #_::place-invalid #_::find-place
              #_::scv-found #_::order-scv
              #_::scv-lost #_::find-scv
              #_::building-started #_::on-building-started
              #_::building-finished #_::on-building-finished}
   :xform xform})

(deftest test-handle-message
  (testing "handle-message"
    (testing "handles empty messages"
      (is (= ::build-something
             (::proc/message-type (handle-message {})))))))

(deftest test-xform
  (testing "xform"
    (testing "works with into"
      (is (= ::build-something
             (::proc/message-type (into {} xform [{}])))))))

(deftest test-proc-handlers
  (testing "proc"
    (testing "can recursively handle-message itself until completion"
      (is (= ::something-built
             (::proc/message-type
              (loop [n (count (:messages proc))
                     message (handle-message {})]
                (if (<= n 0) message
                    (recur (dec n) (handle-message message))))))))))