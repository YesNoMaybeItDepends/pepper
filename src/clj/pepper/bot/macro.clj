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

(defn budget
  "ugly, think about this"
  [our-player unit-jobs]
  (mapv
   #(if (neg? %)
      0
      %)
   (resources/combine-quantities
    -
    (player/resources-available our-player)
    (unit-jobs/total-cost unit-jobs))))

(defn maybe-build-barracks [[macro messages] units our-player unit-jobs frame game-map]
  (let [our-units (filterv unit/exists? (our-units units our-player))
        workers (filterv unit/completed? (workers our-units))
        budget (budget our-player unit-jobs)
        cost (unit-type/cost :barracks)
        can-afford? (resources/can-afford? budget cost)
        some-worker (get-idle-or-mining-worker workers unit-jobs)
        barracks (filterv (unit/type? :barracks) our-units)
        got-enough? (<= 5 (count barracks))
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
                 (< (count current-supply-jobs) 1) ;; fix this
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

(defn have-min-units? "clean up" [want unit-count-by-type]
  (every?
   (fn [[unit min]] (<= min (or (unit unit-count-by-type) 0)))
   (get-in want [:when :have :units])))

(defn got-enough? "clean up" [want unit-count-by-type]
  (<= (:max want)
      (or ((:building want) unit-count-by-type) 0)))

(defn maybe-build [[macro messages] game unit-jobs]
  (let [our-player (game/our-player game)
        budget (budget our-player unit-jobs)
        our-units (filterv unit/exists? (game/our-units game))
        by-type (group-by unit/type our-units)
        counts (reduce-kv
                (fn [m k v]
                  (assoc m k (count v)))
                {}
                by-type)
        wants [{:building :engineering-bay
                :max 1
                :when {:have {:units {:barracks 1
                                      :refinery 1}}}}
               ;;  {:building :academy
               ;;   :max 1
               ;;   :when {:have {:units {:barracks 1
               ;;                         :refinery 1}}}}
               ;;  {:building :barracks
               ;;   :max 4
               ;;   :when {:have {:units {:supply-depot 1}}}}
               ]
        wants-to-build (filterv
                        (fn [want]
                          (and (not (got-enough? want counts))
                               (have-min-units? want counts)
                               (not (already-building? (:building want) unit-jobs))
                               (resources/can-afford? budget (unit-type/cost (:building want)))))
                        wants)
        to-build (:building (first wants-to-build))
        builder (get-idle-or-mining-worker (filterv (every-pred unit/completed?) (:scv by-type)) unit-jobs)]
    (if (and (some? to-build)
             (some? builder))
      (->result macro (job/init (build/job (unit/id builder) to-build)
                                (game/frame game)))
      (->result macro))))

(defn maybe-research "refactor" [[macro messages] game unit-jobs]
  (let [frame (game/frame game)
        our-player (game/our-player game)
        budget (budget our-player unit-jobs)
        research-unit-jobs (filterv (job/type? :research) unit-jobs)
        jobs-by-research (group-by research/to-research research-unit-jobs)
        idle-units-by-type (group-by
                            unit/type
                            (filterv
                             (every-pred #(nil? (some
                                                 (comp #{(unit/id %)} job/unit-id)
                                                 unit-jobs))
                                         unit/completed?
                                         unit/exists?)
                             (game/our-units game)))
        abilities-want [:stim-packs #_:optical-flare] ;; TODO: only reseasrch flare after u-238-shells
        upgrades-want [[:u-238-shells 1] [:terran-infantry-weapons 1] [:terran-infantry-armor 1] #_[:caduceus-reactor 1]]
        mapped-abilities (mapv
                          #(hash-map
                            :ability %
                            :researches (ability/researches %)
                            :some-idle-researcher? (not-empty ((ability/researches %) idle-units-by-type))
                            :have? (player/has-researched? our-player %)
                            :affordable? (resources/can-afford? budget (ability/cost %))
                            :researching? (not-empty (jobs-by-research %)))
                          abilities-want)
        mapped-upgrades (mapv
                         (fn [[x lvl]]
                           (hash-map
                            :upgrade [x lvl]
                            :researches (upgrade/researches x)
                            ;; :can-upgrade? (game/can-upgrade? game x) ;; ?
                            :some-idle-researcher? (not-empty ((upgrade/researches x) idle-units-by-type))
                            #_:have? #_(player/has-upgraded? our-player x)
                            :have? (player/has-upgraded-level? our-player [x lvl])
                            :affordable? (resources/can-afford? budget (upgrade/cost x lvl))
                            :researching? (not-empty (jobs-by-research x))))
                         upgrades-want)
        researchable-abilities (filterv
                                (every-pred
                                 (complement :have?)
                                 (complement :researching?)
                                 :some-idle-researcher?
                                 #_:affordable?)
                                mapped-abilities)
        researchable-upgrades (filterv
                               (every-pred
                                (complement :have?)
                                (complement :researching?)
                                :some-idle-researcher?
                                #_:affordable?
                                #_:can-upgrade?)
                               mapped-upgrades)
        target (cond (not-empty researchable-abilities) (first researchable-abilities)
                     (not-empty researchable-upgrades) (first researchable-upgrades)
                     :else nil)
        researcher (when target (first ((:researches target) idle-units-by-type)))]
    (if (and (some? target)
             (some? researcher))
      (->result macro (job/init (research/job (unit/id researcher) (if (:ability target)
                                                                     {:target (:ability target)}
                                                                     {:target (first (:upgrade target))
                                                                      :level (second (:upgrade target))})) frame))
      (->result macro))))

;; make generic fn checking if we have techs and units

(defn train-medics? [marines medics academies]
  (and (pos? (or academies 0))
       (some? marines)
       (or (nil? medics)
           (zero? medics)
           (and (every? pos? [marines medics])
                (< 12 (/ marines medics))))))

(defn train-firebats? [marines firebats academies techs jobs]
  (let [techs (into [] conj (flatten [techs (mapv research/to-research jobs)]))]
    (and (pos? (or academies 0))
         #_(every? #(some #{%} techs) [:stim-packs :u-238-shells])
         (some? marines)
         (or (nil? firebats)
             (zero? firebats)
             (and (every? pos? [marines firebats])
                  (< 12 (/ marines firebats)))))))

(defn maybe-train-units "refactor" [[macro messages] units our-player unit-jobs frame]
  (let [our-units (filterv (every-pred unit/completed? unit/exists?) (our-units units our-player))
        max-to-train {:scv 30}
        unit-counts (frequencies (mapv unit/type our-units))
        techs (into [] conj (flatten [(player/has-researched our-player)
                                      (player/has-upgraded-max our-player)]))
        trains (merge {:barracks (filterv some? [(when (train-medics? (:marine unit-counts)
                                                                      (:medic unit-counts)
                                                                      (:academy unit-counts))
                                                   :medic)
                                                 #_(when (train-firebats? (:marine unit-counts)
                                                                          (:firebat unit-counts)
                                                                          (:academy unit-counts)
                                                                          techs
                                                                          unit-jobs)
                                                     :firebat)
                                                 :marine])}
                      (when (< (:scv unit-counts)
                               (:scv max-to-train))
                        {:command-center :scv}))
        trainer-types (set (keys trains))
        trainers (idle-units (filterv (unit/type? trainer-types) our-units)
                             unit-jobs)
        budget (budget our-player unit-jobs)
        [remaining-budget new-training-jobs] (reduce
                                              (fn [[budget jobs] trainer]
                                                (let [list-to-train (mapv
                                                                     #(hash-map :to-train % :to-pay (unit-type/cost %))
                                                                     (flatten [((unit/type trainer) trains)]))
                                                      {:keys [to-train to-pay]} (first (filterv
                                                                                        (fn [{:keys [to-train to-pay]}]
                                                                                          (resources/can-afford? budget to-pay))
                                                                                        list-to-train))]
                                                  (if to-train
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
        [macro new-build-jobs] (maybe-build [macro messages] game unit-jobs)
        [macro new-research-jobs] (maybe-research [macro messages] game unit-jobs)
        messages (->> (into (or messages [])
                            (concat new-mining-jobs
                                    new-academy-jobs
                                    new-refinery-jobs
                                    new-training-jobs
                                    new-building-jobs
                                    new-build-rax-jobs
                                    new-build-jobs
                                    new-research-jobs))
                      (filterv (comp some? :unit-id)))]
    [macro messages]))