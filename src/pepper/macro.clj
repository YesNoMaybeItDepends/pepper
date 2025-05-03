(ns pepper.macro
  (:require
   [pepper.macro.budget :as budget]
   [pepper.jobs :as jobs]
   [pepper.bwapi.impl.game :as game]
   [pepper.strategy.spam-scvs :as spam-scvs]
   [pepper.strategy.spam-supply :as spam-supply]
   [pepper.strategy.spam-marines :as spam-marines]
   [pepper.jobs.build :as build]))

(defn index-by-uuid
  "Generates a map of {uuid job}"
  [job]
  {(:uuid job) job})

(defn test-job
  "Generates a single job"
  [& [building worker]]
  {:uuid (clojure.core/random-uuid) :building building :worker worker})

(defn test-job-list
  "Generates a list of jobs"
  [n]
  (for [n (range n)]
    (test-job)))

(defn test-job-map
  "Generates a map of jobs indexed by uuid"
  [n]
  (let [map {}]
    (for [n (range n)]
      (into map (index-by-uuid (test-job))))))

(defn add-job-to-map
  "Adds non indexed job to map"
  [jobs job]
  (conj jobs (index-by-uuid job)))

(defn add-jobs-to-map
  "Adds list of non indexed jobs to map"
  [jobs jobs-to-add]
  (reduce (fn [acc curr] (assoc acc (:uuid curr) curr)) jobs jobs-to-add))

(defn add-indexed-jobs-to-map
  "(unfinished) Adds map of indexed jobs to map"
  [jobs jobs-to-add]
  (into {} (test-job-map 3)))

(defn jobs-by-uuid "Returns map of jobs by uuid" [] @jobs/by-uuid)
(defn jobs "Returns list of jobs" [] (vals @jobs/by-uuid))

(defn request-supply! []
  (jobs/add! (build/job :supply-depot)))

(defn run [game]
  ;; TODO: bw/build rax 
  (budget/run-frame game)
  (game/draw-text-screen game 200 200 (str "Money: " (get-in (budget/get-budget) [:total :minerals])))
  (spam-scvs/maybe-train-workers game)
  (spam-supply/maybe-build-supply game (jobs))
  (spam-marines/maybe-build-barracks game (jobs))
  (jobs/run-jobs! game (jobs)))