(ns test.pepper.proc.proc-test
  (:require [clojure.test :refer [deftest is testing]]
            [pepper.proc.proc :as p]
            [pepper.proc.lab :as l]
            [pepper.proc.hello-world :as hw]
            [pepper.proc.queue :as queue]))

(def AUTO-RUN (atom false))

(defonce procs
  (when @AUTO-RUN
    (atom {:hello-world
           (p/init {:proc hw/proc
                    :pub queue/by-topic
                    :output queue/queue})})))

(def test-message {:hello-world {::p/message-type ::hw/hello}
                   :hopefully-nothing {::p/message-type :hopefully-nothing}
                   :empty {}})

(l/post-message (:empty test-message))

(deftest test-next-message-type
  (testing "next-message-type"
    (let [proc {:messages {:one :two}}
          msg {::p/message-type :one}]
      (testing "returns the next message type as a map")
      (is (= {::p/message-type :two}
             (p/next-message-type msg proc))))))

#_(deftest test-compose-xf
    (testing "compose-xf"
      (testing "takes only xf succesfully"
        (is (= identity (compose-xf identity))))

      (testing "takes a map with the keys :before :xf, :after"
        (is (= identity (compose-xf identity {:before [] :after []}))))

      (testing "composes the transducers in the right order"
        (is (= ((compose-xf
                 #(conj % 1)
                 {:before [(fn [x] (conj x 0))]
                  :after [(fn [x] (conj x 2))]}) [])
               [0 1 2])))))


