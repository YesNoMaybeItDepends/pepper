(ns pepper.bot.jobs.research
  (:require
   [pepper.api :as api]
   [pepper.game.upgrade :as upgrade]
   [pepper.game.ability :as ability]
   [pepper.bot.job :as job])
  (:import
   [bwapi Game Unit]))

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
    (if (or (not researching?)
            (not upgrading?))
      (job/set-completed job)
      job)))

(defn start-research! [job api]
  (let [game (api/game api)
        frame (Game/.getFrameCount game)
        unit (Game/.getUnit game (job/unit-id job))
        started-research? (frame-started-research job)
        to-upgrade (when (not started-research?) (upgrade/kw->obj (to-research job)))
        can-upgrade? (when to-upgrade (Unit/.canUpgrade unit to-upgrade))
        to-research (when (not started-research?) (ability/kw->obj (to-research job)))
        can-research? (when to-research (Unit/.canResearch unit to-research))]
    (if (or can-upgrade? can-research?)
      (do (cond
            can-upgrade? (Unit/.upgrade unit to-upgrade)
            can-research? (Unit/.research unit to-research))
          (-> job
              (set-frame-started-research frame)
              (job/set-cost-paid frame)
              (job/set-step :done-researching?!)))
      job)))

(defn xform
  [[job api]]
  (case (:step job)
    :start-research! (#'start-research! job api)
    :done-researching?! (#'done-researching? job api)
    job))

(defn job [unit-id to-research]
  (job/register-xform! :research xform)
  (-> {:job :research
       :xform-id :research ;; if I check if a job has an xform then i dont need this
       :step :start-research!
       :to-research to-research
       :unit-id unit-id}
      (job/set-cost (upgrade/cost to-research))))

(def job-def
  {:job :research
   :requires [:unit-id :to-research]
   :steps {:start-research! :is-researching?!}})

