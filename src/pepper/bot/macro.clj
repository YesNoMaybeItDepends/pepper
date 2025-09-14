(ns pepper.bot.macro
  (:require
   [clojure.set :as sql]
   [pepper.bot.job :as job]
   [pepper.bot.jobs.build :as build]
   [pepper.bot.jobs.gather :as gather]
   [pepper.bot.jobs.research :as research]
   [pepper.bot.jobs.train :as train]
   [pepper.bot.macro.auto-supply :as auto-supply]
   [pepper.bot.macro.mining :as mining]
   [pepper.bot.unit-jobs :as unit-jobs]
   [pepper.game :as game]
   [pepper.game.ability :as ability]
   [pepper.game.map :as game-map]
   [pepper.game.player :as player]
   [pepper.game.resources :as resources]
   [pepper.game.unit :as unit]
   [pepper.game.unit-type :as unit-type]
   [pepper.game.upgrade :as upgrade]))

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
  (filterv gather/gather-job? unit-jobs))

(defn building-jobs [unit-jobs]
  (filterv (job/type? :build) unit-jobs))

(defn mining-workers [workers unit-jobs]
  (filterv #(unit-has-any-job? % (mining-jobs unit-jobs)) workers))

(defn get-idle-or-mining-worker [workers unit-jobs]
  (or (first (idle-units workers unit-jobs))
      (first (mining-workers workers unit-jobs))))

(defn already-building? [building unit-jobs]
  (some #(#{building} (build/building %)) unit-jobs))

(defn budget [our-player unit-jobs]
  (resources/combine-quantities
   -
   (player/resources-available our-player)
   (unit-jobs/total-cost unit-jobs)))

(defn maybe-build-barracks [[macro messages] units our-player unit-jobs frame game-map]
  (let [our-units (filterv unit/exists? (our-units units our-player))
        workers (filterv unit/completed? (workers our-units))
        budget (budget our-player unit-jobs)
        cost (unit-type/cost :barracks)
        can-afford? (resources/can-afford? budget cost)
        some-worker (get-idle-or-mining-worker workers unit-jobs)
        barracks (filterv (unit/type? :barracks) our-units)
        got-enough? (<= 6 (count barracks))
        our-base (game-map/get-base-by-id game-map (player/starting-base our-player))
        near-tile (:center our-base)
        current-barracks-jobs (transduce
                               (comp
                                (filter (comp #{:build} :job))
                                (filter (comp #{:barracks} build/building)))
                               conj
                               []
                               unit-jobs)]
    (if (and (not got-enough?)
             (< (count current-barracks-jobs)
                1)
             some-worker
             can-afford?)
      (->result macro (job/init (build/job (unit/id some-worker) :barracks {:near-tile near-tile}) frame))
      (->result macro))))

(defn maybe-build-supply [[macro messages] units our-player unit-jobs frame]
  (let [our-units (filterv unit/exists? (our-units units our-player))
        workers (filterv unit/completed? (workers our-units))
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
            (and can-afford?
                 (< (count current-supply-jobs) 1)
                 (<= 3 (count barracks))))
      (let [worker (get-idle-or-mining-worker workers unit-jobs)
            new-job (job/init (build/job (unit/id worker) :supply-depot) frame)]
        (->result macro new-job))
      (->result macro))))

(defn maybe-build-refinery [[macro messages] units our-player unit-jobs frame game-map]
  (let [our-main (game-map/get-base-by-id game-map (player/starting-base our-player))
        geysers (set (:geysers our-main))
        geysers (filterv (every-pred (comp geysers unit/id)
                                     (unit/type? :vespene-geyser)) units)
        can-afford? (resources/can-afford?
                     (budget our-player unit-jobs)
                     (unit-type/cost :refinery))
        refinery-jobs (->> (building-jobs unit-jobs)
                           (filterv #(= (build/building %)
                                        :refinery)))
        our-units (filterv unit/exists? (our-units units our-player))
        counts (group-by :type our-units)
        count-depots (count (:supply-depot counts))
        count-barracks (count (:barracks counts))
        worker (get-idle-or-mining-worker (filterv (every-pred unit/completed? unit/exists? (unit/type? :scv)) our-units) unit-jobs)]
    (if (and (not-empty geysers)
             can-afford?
             (empty? refinery-jobs)
             (<= 1 count-depots)
             (<= 1 count-barracks)
             (some? worker))
      (->result macro (job/init (build/job (unit/id worker) :refinery {:geyser-id (unit/id (first geysers))}) frame))
      (->result macro))))

(defn maybe-build-academy [[macro messages] units our-player unit-jobs frame]
  (let [our-units (filterv unit/exists? (our-units units our-player))
        no-academy? (nil? (first (filterv (unit/type? :academy) our-units)))
        already-building? (->> (building-jobs unit-jobs)
                               (filterv #(= (build/building %)
                                            :academy))
                               not-empty)
        can-afford? (resources/can-afford?
                     (budget our-player unit-jobs)
                     (unit-type/cost :academy))
        refinery? (first (filterv (unit/type? :refinery) our-units))
        worker (get-idle-or-mining-worker (filterv (every-pred unit/completed? unit/exists? (unit/type? :scv)) our-units) unit-jobs)]
    (if (and no-academy?
             refinery?
             (not already-building?)
             can-afford?)
      (->result macro (job/init (build/job (unit/id worker) :academy) frame))
      (->result macro))))

(defn maybe-research [[macro messages] game unit-jobs]
  (let [frame (game/frame game)
        our-player (game/our-player game)
        budget (budget our-player unit-jobs)
        academy (first
                 (filterv
                  (every-pred unit/exists? unit/completed? (unit/type? :academy))
                  (game/our-units game)))
        abilities-want [:stim-packs]
        upgrades-want [:u-238-shells]
        abilities-have (filterv #(player/has-researched? our-player %) abilities-want)
        upgrades-have (filterv #(player/has-upgraded? our-player %) upgrades-want)
        abilities-need (sql/difference (set abilities-want) (set abilities-have))
        upgrades-need (sql/difference (set upgrades-want) (set upgrades-have))
        abilities-need-can-afford (filterv (comp #(resources/can-afford? budget %) ability/cost) abilities-need)
        ;; not taking into account upgrade levels, but this will be refactored anyways
        upgrades-need-can-afford (filterv (comp #(resources/can-afford? budget %) upgrade/cost) upgrades-need)
        researching? (not-empty (filterv (job/type? :research) unit-jobs))
        target (cond (not-empty abilities-need-can-afford) (first abilities-need-can-afford)
                     (not-empty upgrades-need-can-afford) (first upgrades-need-can-afford)
                     :else nil)]
    (if (and academy
             (not researching?)
             target)
      (->result macro (job/init (research/job (unit/id academy) target) frame))
      (->result macro))))

(defn train-medics? [marines medics academies]
  (and (pos? (or academies 0))
       (some? marines)
       (or (nil? medics)
           (zero? medics)
           (and (every? pos? [marines medics])
                (< 12 (/ marines medics))))))

(defn train-firebats? [marines firebats techs]
  (and (every? #(some #{%} techs) [:stim-packs :u-238-shells])
       (some? marines)
       (or (nil? firebats)
           (zero? firebats)
           (and (every? pos? [marines firebats])
                (< 12 (/ marines firebats))))))

(defn maybe-train-units [[macro messages] units our-player unit-jobs frame]
  (let [our-units (filterv unit/exists? (our-units units our-player))
        max-to-train {:scv 25}
        unit-counts (frequencies (mapv unit/type our-units))
        techs (into [] conj (flatten [(player/has-researched our-player)
                                      (player/has-upgraded-max our-player)]))
        trains (merge {:barracks (filterv some? [:marine
                                                 (when (train-medics? (:marine unit-counts)
                                                                      (:medic unit-counts)
                                                                      (:academy unit-counts))
                                                   :medic)
                                                 (when (train-firebats? (:marine unit-counts)
                                                                        (:firebat unit-counts)
                                                                        techs)
                                                   :firebat)])}
                      (when (< (:scv unit-counts)
                               (:scv max-to-train))
                        {:command-center :scv}))
        trainer-types (set (keys trains))
        trainers (idle-units (filterv (unit/type? trainer-types) our-units)
                             unit-jobs)
        budget (budget our-player unit-jobs)
        [remaining-budget new-training-jobs] (reduce
                                              (fn [[budget jobs] trainer]
                                                (let [to-train (rand-nth (flatten [((unit/type trainer) trains)]))
                                                      to-pay (unit-type/cost to-train)]
                                                  (if (resources/can-afford? budget to-pay)
                                                    [(resources/combine-quantities - budget to-pay)
                                                     (conj jobs (-> (train/job (unit/id trainer) to-train)
                                                                    (job/init frame)))]
                                                    (reduced [budget jobs]))))
                                              [budget []]
                                              trainers)]
    [macro new-training-jobs]))

(defn handle-idle-workers [[macro messages] units our-player unit-jobs frame game-map]
  (let [mineral-field-ids (transduce
                           (comp
                            (filter (every-pred :completed? :visible?
                                                (unit/type? unit-type/mineral-field)))
                            (filter (comp (every-pred some? pos?) unit/resources))
                            (map unit/id))
                           conj
                           []
                           units)
        our-units (filterv (every-pred unit/completed? unit/exists?) (our-units units our-player))
        idle-worker-ids (mapv unit/id (idle-units (workers our-units) unit-jobs))
        refinery-ids (transduce
                      (comp
                       (filter (every-pred unit/completed? (unit/type? :refinery)))
                       (map unit/id))
                      conj
                      []
                      our-units)
        workers-per-refinery (transduce
                              (comp
                               (filter gather/gather-job?)
                               (filter (comp (set refinery-ids) gather/target-id)))
                              conj
                              []
                              unit-jobs)
        [to-gas to-minerals] (if (pos? (count refinery-ids))
                               (split-at (- 1 (count workers-per-refinery)) idle-worker-ids)
                               [[] (or idle-worker-ids [])])
        new-gas-jobs     (mining/new-mining-jobs to-gas      refinery-ids frame)
        new-mineral-jobs (mining/new-mining-jobs to-minerals mineral-field-ids frame)]
    (->result macro (concat new-gas-jobs new-mineral-jobs))))

(defn update-on-frame [[macro messages] game unit-jobs]
  (let [our-player (player/our-player (game/players game))
        units (game/units game)
        frame (game/frame game)
        game-map (game/get-map game)
        [macro new-mining-jobs] (handle-idle-workers [macro messages] units our-player unit-jobs frame game-map)
        [macro new-refinery-jobs] (maybe-build-refinery [macro messages] units our-player unit-jobs frame game-map)
        [macro new-academy-jobs] (maybe-build-academy [macro messages] units our-player unit-jobs frame)
        [macro new-training-jobs] (maybe-train-units [macro messages] units our-player unit-jobs frame)
        [macro new-building-jobs] (maybe-build-supply [macro messages] units our-player unit-jobs frame)
        [macro new-build-rax-jobs] (maybe-build-barracks [macro messages] units our-player unit-jobs frame game-map)
        [macro new-research-jobs] (maybe-research [macro messages] game unit-jobs)
        messages (->> (into (or messages [])
                            (concat new-mining-jobs
                                    new-academy-jobs
                                    new-refinery-jobs
                                    new-training-jobs
                                    new-building-jobs
                                    new-build-rax-jobs
                                    new-research-jobs))
                      (filterv (comp some? :unit-id)))]
    [macro messages]))