(ns pepper.mocking
  (:import (org.mockito Mockito)))

(defn when-then [x w t]
  (-> (w x)
      (Mockito/when)
      (.thenReturn t)
      (.getMock)))

(defn mock [class opts]
  (reduce (fn [acc [m v]]
            (when-then acc m v))
          (Mockito/mock class)
          opts))