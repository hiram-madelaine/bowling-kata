(ns bowling-kata.cards
  (:require-macros
    ;; Notice that I am not including the 'devcards.core namespace
    ;; but only the macros. This helps ensure that devcards will only
    ;; be created when the :devcards is set to true in the build config.
    [devcards.core :as dc :refer [defcard defcard-doc defcard-om-next noframe-doc deftest dom-node]])
  (:require [bowling-kata.boot :as bow]))

(enable-console-print!)



(defcard frame-with-spare
         "Frame with spare bonus"
         (let [frame {:id 1
                      :rolls [8 2]
                      :score 10}]
           (bow/frame frame)))

(defcard frame-with-strike
         "Frame with strike bonus"
         (let [frame {:rolls [10]
                      :id 2
                      :score 29}]
           (bow/frame frame)))

(defcard frame-without-bonus
         "Frame with spare bonus"
         (let [frame {:rolls [8 1]
                      :id 3
                      :score 19}]
           (bow/frame frame)))


(defcard tenieth-frame
         "Extra ball"
         (let [frame {:rolls [8 2 10]
                      :id 10
                      :score 19}]
           (bow/frame frame)))