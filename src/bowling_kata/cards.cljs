(ns bowling-kata.cards
  (:require-macros
    [devcards.core :refer [defcard]])
  (:require [bowling-kata.boot :as bow]))


(enable-console-print!)

(devcards.core/start-devcard-ui!)

(defcard my-first-card
         "Frame"
         (let [rolls [8 2]]
           (bow/frame rolls)))