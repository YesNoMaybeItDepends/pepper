(ns pepper.game.color
  (:refer-clojure :exclude [key])
  (:require [clojure.set :as sql])
  (:import [bwapi Color]))

(def ^:private  by-keyword {:green Color/Green
                            :black Color/Black
                            :brown Color/Brown
                            :cyan Color/Cyan
                            :yellow Color/Yellow
                            :grey Color/Grey
                            :teal Color/Teal
                            :orange Color/Orange
                            :blue Color/Blue
                            :purple Color/Purple
                            :white Color/White
                            :red Color/Red})

(def ^:private by-object (sql/map-invert by-keyword))

(defn object->keyword [api-color]
  (get by-object api-color))

(defn keyword->object [color]
  (get by-keyword color))