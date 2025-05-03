(ns pepper.debug.logger
  (:require [pepper.bwapi.impl.game :as game]
            [pepper.jobs :as jobs]))

(defn logger-handler [game text]
  (cond (= text "game") (tap> {:game game
                               :player (game/self game)})
        (= text "jobs") (tap> @jobs/by-uuid)))