(ns pepper.debug.logger
  (:require [pepper.api.game :as game]
            [pepper.jobs :as jobs]))

#_(defn logger-handler [game text]
    (cond (= text "game") (tap> {:game game
                                 :player (game/self)})
          (= text "jobs") (tap> @jobs/by-uuid)))