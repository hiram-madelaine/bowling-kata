(ns ^:figwheel-always bowling-kata.cards
  (:require-macros
    ;; Notice that I am not including the 'devcards.core namespace
    ;; but only the macros. This helps ensure that devcards will only
    ;; be created when the :devcards is set to true in the build config.
    [devcards.core :as dc :refer [defcard defcard-doc defcard-om-next noframe-doc deftest dom-node mkdn-pprint-source]])
  (:require [bowling-kata.boot :as ui]
            [bowling-kata.core :as bow]
            [schema.core :as s]
            [schema.test :refer [validate-schemas]]
            [cljs.test :as test :refer-macros [is are testing use-fixtures]]))

(enable-console-print!)


(use-fixtures :once {:before (fn []
                               validate-schemas)})


(defcard-doc
  "##  Welcome to my Bowling Kata report
   ### I will explain my way to solve this problem with my favorite libraries :
   - Prismatic/schema to shape the domain model
   - core.match to exprress business rules
   - specter to manipulate the domain model
   - Om.Next to visualise
   - DevCards ")


(defcard-doc
  "### The model
  I use Prismatic/schema to shape the domain model and constraints.
  The 10nth frame is very particular and breaks nice properties of the 9 first.
  I think the best way to model this problem is to have an extra Frame with one roll."
  (mkdn-pprint-source bow/Pins)
  "- The rolls for a frame"
  (mkdn-pprint-source bow/FrameRolls)

  (mkdn-pprint-source bow/Bonus)

  (mkdn-pprint-source bow/Frame)

  (mkdn-pprint-source bow/Game))



(deftest test-score-per-frame
         (testing "Testing score per frame in isolation of other frames"
           (is (= 7 (bow/frame-score [7])) "Score with one rolls")
           (is (= 9 (bow/frame-score [7 2])) "Score with two rolls")
           (is (= 10 (bow/frame-score [10])) "Score with one strike")
           (is (= 10 (bow/frame-score [8 2])) "Score with one spare")
           (is (= 20 (bow/frame-score [9 1 10])) "Score with 3 rolls")))


(deftest test-bonus
         (testing "Testing the determination of a bonus"
           (is (= nil (bow/bonus [1 2])))
           (is (= :spare (bow/bonus [8 2])))
           (is (= :strike (bow/bonus [10])))))


(deftest test-scores
         (testing "Scoring of all frames - Scores are cumulatives"
           (is (= [9] (bow/scores [[8 1]])) "Score of starting game")
           (is (= [9 19] (bow/scores [[8 1] [9 1]])) "On going match")
           (is (= [9 29 39] (bow/scores [[8 1] [9 1] [10]])))
           (is (= [14 19 24] (bow/scores [[8 2] [4 1] [4 1]])) "First frame is a spare")
           (is (= [15 20 25] (bow/scores [[10] [4 1] [4 1]])) "First frame is a strike")))


(defcard frame-with-spare
         "Frame with spare bonus"
         (let [frame {:id 1
                      :rolls [8 2]
                      :score 10}]
           (ui/frame frame)))

(defcard frame-with-strike
         "Frame with strike bonus"
         (let [frame {:rolls [10]
                      :id 2
                      :score 29}]
           (ui/frame frame)))

(defcard frame-without-bonus
         "Frame with spare bonus"
         (let [frame {:rolls [8 1]
                      :id 3
                      :score 19}]
           (ui/frame frame)))


(defcard tenieth-frame
         "Extra ball"
         (let [frame {:rolls [8 2 10]
                      :id 10
                      :score 19}]
           (ui/frame frame)))


(deftest frame-is-done?
         "## Test if a frame is done or not
                  In the dynamic of the game I must know if a frame is done and if we have to go to next frame "
         (testing
           (are [frame result] (= (bow/frame-done? frame) result)
                               {:rolls  [] :id 1} false
                               {:rolls  [1] :id 1} false
                               {:rolls [1 3] :id 1} true
                               {:rolls [] :id 10}  false
                               {:rolls [1 2] :id 10}  true
                               {:rolls [10] :id 9} true
                               {:rolls [10] :id 10} false
                               {:rolls [10 2] :id 10} false
                               {:rolls [10 10 2] :id 10} true
                               {:rolls [8 2] :id 10} false
                               {:rolls [8 2 7] :id 10} true)))