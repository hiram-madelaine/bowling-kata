(ns bowling-kata.t-core
  (:use midje.sweet)
  (:require [bowling-kata.core :as bow]
            [schema.core :as s]
            [schema.test]))


(s/with-fn-validation
  (fact "Determine if there is a bonus for the frame"
        (bow/bonus [1 2]) => nil
        (bow/bonus [8 2]) => :spare
        (bow/bonus [10]) => :strike))


(fact "Detemine score per frame"
      (bow/frame-score [8 1 9]) => 9
      (bow/frame-score [9 1 10]) => 20
      (bow/frame-score [10 10 8]) => 28
      (bow/frame-score [10]) => 10)


(s/with-fn-validation
  (fact "Start Game score"
        (bow/game-score [[8 1]]) => [9]))


(s/with-fn-validation
  (fact "Ongoing Game score"
        (bow/game-score [[8 1] [9 1]]) => [9 19]
        (bow/game-score [[8 1] [9 1] [10]]) => [9 29 39]))

(def game [[8 1] [9 1] [10] [10] [8 1] [7 2] [10] [10] [10] [8 2 9]])


(s/with-fn-validation
  (fact "Cumultative Game score"
       (bow/game-score game) => [9 29 57 76 85 94 124 152 172 191]))


(fact "Total score"
      (bow/total-score game) => 191)
