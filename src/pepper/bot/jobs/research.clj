(ns pepper.bot.jobs.research
  (:require
   [pepper.api :as api]
   [pepper.bot.job :as job]
   [pepper.game.ability :as ability]
   [pepper.game.upgrade :as upgrade])
  (:import
   [bwapi
    Game
    Player
    Unit
    UpgradeType]))

(defn to-research [job]
  (:to-research job))

(defn frame-started-research [job]
  (:frame-started-research job))

(defn set-frame-started-research [job frame]
  (assoc job :frame-started-research frame))

(defn done-researching? [job api]
  (let [game (api/game api)
        unit (Game/.getUnit game (job/unit-id job))
        researching? (Unit/.isResearching unit)
        upgrading? (Unit/.isUpgrading unit)]
    (if (and (not researching?)
             (not upgrading?))
      (job/set-completed job)
      job)))

(defn start-research! [job api]
  (let [game (api/game api)
        frame (Game/.getFrameCount game)
        unit (Game/.getUnit game (job/unit-id job))
        our-player (Game/.self game)
        started-research? (frame-started-research job)
        to-upgrade (when (not started-research?) (upgrade/kw->obj (to-research job)))
        can-upgrade? (when to-upgrade (Unit/.canUpgrade unit to-upgrade))
        to-research (when (not started-research?) (ability/kw->obj (to-research job)))
        can-research? (when to-research (Unit/.canResearch unit to-research))
        already-researched? (when to-research (Player/.hasResearched our-player to-research))
        already-upgraded? (when to-upgrade (= (UpgradeType/.maxRepeats to-upgrade)
                                              (Player/.getUpgradeLevel our-player to-upgrade)))]
    (if (or can-upgrade? can-research?)
      (do (cond
            can-upgrade? (Unit/.upgrade unit to-upgrade)
            can-research? (Unit/.research unit to-research))
          (-> job
              (set-frame-started-research frame)
              (job/set-cost-paid frame)
              (job/set-step :done-researching?!)))
      (if (or already-researched? already-upgraded?)
        (do (println "bro something went wrong...")
            (job/set-completed job))
        job))))

(defn xform
  [[job api]]
  (case (:step job)
    :start-research! (#'start-research! job api)
    :done-researching?! (#'done-researching? job api)
    job))

(defn job [unit-id {:keys [target level]}]
  (job/register-xform! :research #'xform)
  (-> {:job :research
       :xform-id :research ;; if I check if a job has an xform then i dont need this
       :step :start-research!
       :to-research target
       :to-research-level level
       :unit-id unit-id}
      (job/set-cost (cond
                      (target upgrade/by-keyword) (upgrade/cost target level)
                      (target ability/by-keyword) (ability/cost target)))))

(def job-def
  {:job :research
   :requires [:unit-id :to-research]
   :steps {:start-research! :is-researching?!}})

