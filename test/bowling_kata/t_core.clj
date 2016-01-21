(ns bowling-kata.t-core
  (:use midje.sweet)
  (:require [bowling-kata.core :as bow :relaod true]
            [schema.core :as s]
            [schema.test]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop :include-macros true]))


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
        (bow/scores [[8 1]]) => [9]))

(s/with-fn-validation
  (fact "Ongoing Game score"
        (bow/scores [[8 1] [9 1]]) => [9 19]
        (bow/scores [[8 1] [9 1] [10]]) => [9 29 39]))

(def all-rolls [[8 1] [9 1] [10] [10] [8 1] [7 2] [10] [10] [10] [8 2 9]])



(s/with-fn-validation
  (fact "Cumultative Game score"
        (bow/scores all-rolls) => [9 29 57 76 85 94 124 152 172 191]))



(defn only [pins-down]
  (into [] (repeat 10 [pins-down pins-down])))

(def spare-ex [[1 1] [1 1] [1 1] [1 1] [8 2] [1 1] [1 1] [1 1] [1 1] [1 1]])

(def strike-ex [[1 1] [1 1] [1 1] [1 1] [10] [1 1] [1 1] [1 1] [1 1] [1 1]])

(def strike-a-la-fin [[2 3] [4 5] [10] [3 4] [5 5] [6 6] [7 7] [8 8] [9 9] [8 2 10]])

(def perfect-game [[10] [10] [10] [10] [10] [10] [10] [10] [10] [10 10 10]])

(def in-middle-game [[3 4] [1 3] [6 0] [10] [] [] [] [] [] []])

(facts "Check our bowling game score calculator"
       (fact "only 0 pins down"                      (bow/score (only 0)) => 0)
       (fact "only 1 pins down"                      (bow/score (only 1)) => 20)
       (fact "only 1 pins down and 1 spare"          (bow/score spare-ex) => 29)
       (fact "only 1 pins down and 1 strike"         (bow/score strike-ex) => 30)
       (fact "all strike "                           (bow/score perfect-game) => 300)
       (fact "a score while in the middle of a game" (bow/score in-middle-game) => 27))


(fact "Frame is finished ?"
      (bow/frame-done? {:rolls  [] :id 1}) => false
      (bow/frame-done? {:rolls  [1] :id 1}) => false
      (bow/frame-done? {:rolls  [1 3] :id 1}) => true
      (bow/frame-done? {:rolls  [] :id 10}) => false
      (bow/frame-done? {:rolls  [1 2] :id 10}) => true
      (bow/frame-done? {:rolls  [10] :id 9}) => true
      (bow/frame-done? {:rolls  [10] :id 10}) => false
      (bow/frame-done? {:rolls  [10 2] :id 10}) => false
      (bow/frame-done? {:rolls  [10 2 8] :id 10}) => true
      (bow/frame-done? {:rolls  [8 2] :id 10}) => false
      (bow/frame-done? {:rolls  [8 2 7] :id 10}) => true)
