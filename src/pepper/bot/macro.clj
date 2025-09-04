(ns pepper.bot.macro
  (:require
   [pepper.bot.job :as job]
   [pepper.bot.jobs.build :as build]
   [pepper.bot.jobs.train :as train]
   [pepper.bot.macro.auto-supply :as auto-supply]
   [pepper.bot.macro.mining :as mining]
   [pepper.game.player :as player]
   [pepper.game.resources :as resources]
   [pepper.game.unit :as unit]
   [pepper.game.unit-type :as unit-type]))

;;;; utils

(defn ->messages [& whatever]
  (into [] (flatten whatever)))

(defn ->result [state & messages]
  [state (->messages (or messages []))])

(defn result->messages [[_ messages]]
  (or messages []))

(defn merge-messages [messages new-messages]
  (->messages messages new-messages))

(defn messages+result->messages [messages result]
  (->messages messages (result->messages result)))

;;;;

(defn our-units [units our-player]
  (filterv #(unit/owned-by-player? % our-player) units))

(defn workers [units]
  (filterv (unit/type? unit-type/worker) units))

(defn mineral-fields [units]
  (filterv (unit/type? unit-type/mineral-field) units))

(defn job-for-unit? [job unit]
  (= (job/unit-id job) (unit/id unit)))

(defn unit-has-any-job? [unit jobs]
  (some #(job-for-unit? % unit) jobs))

(defn idle-units [units unit-jobs]
  (filterv #(not (unit-has-any-job? % unit-jobs)) units))

(defn mining-jobs [unit-jobs]
  (filterv (job/type? :mining) unit-jobs))

(defn building-jobs [unit-jobs]
  (filterv (job/type? :build) unit-jobs))

(defn mining-workers [workers unit-jobs]
  (filterv #(unit-has-any-job? % (mining-jobs unit-jobs)) workers))

(defn get-idle-or-mining-worker [workers unit-jobs]
  (or (first (idle-units workers unit-jobs))
      (first (mining-workers workers unit-jobs))))

(defn already-building? [building unit-jobs]
  (some #(#{building} (build/building %)) unit-jobs))

(defn maybe-build-barracks [[macro messages] our-units our-player unit-jobs frame]
  (let [workers (workers our-units)
        budget (player/resources-available our-player)
        cost (unit-type/cost :barracks)
        can-afford? (resources/can-afford? budget cost)
        some-worker (get-idle-or-mining-worker workers unit-jobs)
        barracks (filterv (unit/type? :barracks) our-units)
        got-enough? (<= 6 (count barracks))
        already-building? (already-building? :barracks unit-jobs)]
    (if (and (not got-enough?)
             (not already-building?)
             some-worker
             can-afford?)
      (->result macro (job/init (build/job (unit/id some-worker) :barracks) frame))
      (->result macro))))

(defn maybe-build-supply [[macro messages] our-units our-player unit-jobs frame]
  (let [workers (workers our-units)
        barracks (->> our-units
                      (filterv (unit/type? :barracks)))
        need-supply? (auto-supply/need-supply? our-player)
        can-afford? (resources/can-afford?
                     (player/resources-available our-player)
                     (unit-type/cost :supply-depot))
        current-supply-jobs (->> (building-jobs unit-jobs)
                                 (filterv #(= (build/building %)
                                              :supply-depot)))]
    (if (or (and need-supply?
                 can-afford?
                 (< (count current-supply-jobs) 1))
            (and need-supply?
                 can-afford?
                 (< (count current-supply-jobs) 2)
                 (<= 4 (count barracks))))
      (let [worker (get-idle-or-mining-worker workers unit-jobs)
            new-job (job/init (build/job (unit/id worker) :supply-depot) frame)]
        (->result macro new-job))
      (->result macro))))

(defn maybe-train-units [[macro messages] our-units our-player unit-jobs frame]
  (let [max-to-train {:scv 25}
        unit-counts (frequencies (mapv unit/type our-units))
        trains (merge {:barracks :marine}
                      (when (< (:scv unit-counts)
                               (:scv max-to-train))
                        {:command-center :scv}))
        trainer-types (set (keys trains))
        trainers (idle-units (filterv (unit/type? trainer-types) our-units)
                             unit-jobs)
        budget (player/resources-available our-player)
        [remaining-budget new-training-jobs] (reduce
                                              (fn [[budget jobs] trainer]
                                                (let [to-train ((unit/type trainer) trains)
                                                      to-pay (unit-type/cost to-train)]
                                                  (if (resources/can-afford? budget to-pay)
                                                    [(resources/combine-quantities - budget to-pay)
                                                     (conj jobs (-> (train/job (unit/id trainer) to-train)
                                                                    (job/init frame)))]
                                                    (reduced [budget jobs]))))
                                              [budget []]
                                              trainers)]
    [macro new-training-jobs]))

(defn handle-idle-workers [[macro messages] workers mineral-fields unit-jobs frame]
  (let [idle-workers (idle-units workers unit-jobs)
        idle-worker-ids (mapv unit/id idle-workers)
        mineral-field-ids (transduce
                           (comp
                            (filter #((every-pred some? pos?) (unit/resources %)))
                            (map unit/id))
                           conj []
                           mineral-fields)
        new-mining-jobs (mining/new-mining-jobs idle-worker-ids mineral-field-ids frame)]
    (->result macro new-mining-jobs)))

(defn update-on-frame [[macro messages] {:keys [units players frame unit-jobs]}]
  (let [our-player (player/our-player players)
        our-units (our-units units our-player)
        workers (workers our-units)
        mineral-fields (mineral-fields units)
        [macro new-mining-jobs] (handle-idle-workers [macro messages] workers mineral-fields unit-jobs frame)
        [macro new-training-jobs] (maybe-train-units [macro messages] our-units our-player unit-jobs frame)
        [macro new-building-jobs] (maybe-build-supply [macro messages] our-units our-player unit-jobs frame)
        [macro new-build-rax-jobs] (maybe-build-barracks [macro messages] our-units our-player unit-jobs frame)
        messages (into (or messages []) (concat new-mining-jobs new-training-jobs new-building-jobs new-build-rax-jobs))]
    [macro messages]))