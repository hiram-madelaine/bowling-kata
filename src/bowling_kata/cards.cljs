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
            [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [cljs.test :as test :refer-macros [is are testing use-fixtures]]))

(enable-console-print!)


(use-fixtures :once {:before (fn []
                               validate-schemas)})


(defcard-doc
  "##  Welcome to my [Bowling Kata](https://github.com/jgrodziski/clojure-bowling-game) report
   ### The goal of the Kata is to compute the score of a Bowling Game.

   This Devcards exposes the way I explored and solve this problem.
   In the process of solving this kata, I used some techniques inspired by [DDD](https://domainlanguage.com/ddd/) and
   libraries I am familiar with, when needed :

   - [Prismatic/schema](https://github.com/plumatic/schema) to shape the domain model and validation
   - [core.match](https://github.com/clojure/core.match) to express business rules
   - [specter](https://github.com/nathanmarz/specter) to manipulate the domain model
   - [Om.Next](https://github.com/omcljs/om) for visualization and interactivity
   - CSS Flexbox for positioning.
   - [DevCards](https://github.com/bhauman/devcards) to explore, try, test, explain...


   I spent two hours to come up with a scoring solution, tests included,
   and 5 hours for the scoring simulation using OmNext.
   The solutions are certainly not the best neither the shortest but code golf was not a goal.
   I try to write maintainable code")
(defcard-doc
  "## The problem
  The game consists of **10 frames**. In each frame the player has
  **two** opportunities to **knock down** **10 pins**. The **score** for the frame is the total
  number of pins knocked down, plus bonuses for strikes and spares.


  A **spare** is when the player knocks down all 10 pins in two tries. The **bonus** for that frame is the number of pins knocked down by the next roll. ‘/’ denote a spare in the score sheet.
  A **strike** is when the player knocks down all 10 pins on his first try. The **bonus** for that frame is the value of the next two balls rolled. ‘X’ denote a striker in the score sheet.

  In the **tenth frame** a player who rolls a spare or strike is allowed to roll the **extra balls** to complete the frame (so **3 balls** can be rolled in **tenth frame**).\n")

(defcard-doc
  "## The model

  As a huge fan of **DDD**, I always spend a lot of time studying the vocabulary of the problem.
  I use Prismatic/schema to capture the langage of the domain and shape the entities.
  I change the names until I am satisfied with the meaning.
  The names of the Schema are Ubiquitous in the source code, this is very important to me, because when you write Clojure in the large it is important for maintenance.

   - Naming the number of pins knocked down in a roll gave me a hard time. I tried `Pins` and `Pin` but went back to `Roll`. We only deal with a maximum of 10 pins, so Int is good."
  (mkdn-pprint-source bow/Roll)
  "- The rolls for a frame, must be kept in order and grow on the right-end side. So a vector of Pins is a good choice."
  (mkdn-pprint-source bow/FrameRolls)
  "- A frame may have a bonus : spare or strike"
  (mkdn-pprint-source bow/Bonus)

  "- The score range from 0 to 300 for a perfect game."
  (mkdn-pprint-source bow/Score)

  #_(mkdn-pprint-source bow/Game))



(defcard-doc
  "## Determine the bonus of a Frame.

  As the scoring depends on the bonus of a frame, my first task was to determine if a frame benefits form a bonus.
  My goal is to give the final score but also the score of an ongoing game.
  The determination of the bonus must be handle an uncomplete frame.

  ### The rules :
  - **Strike** : When all **ten pins** are knocked down with the **first ball**, a player is awarded ten points, plus a bonus of whatever is scored with the **next two balls**.
  - **spare** : When no pins are left standing after the **second ball** of a frame, a player is awarded ten points, plus a bonus of whatever is scored with **the next ball**


  When it comes to rule implementation,
   I like the conciseness of core.match that presents the logic in a manner that could be validated by an expert."
  (mkdn-pprint-source bow/bonus)

  "#### How to deal with nil value without too much cluttering the code

  In the bonus function, I had to deal with uncomplete frame and so roll1 and/or roll2 could be nil.
  The functions `+` and `pos?` break with nil parameters.
  I used the `fnil` function to handle this cases : "

  (mkdn-pprint-source bow/add)
  (mkdn-pprint-source bow/pins-down?))



(deftest test-bonus
         (testing "let's test the bonus function"
           (is (= nil (bow/bonus [])) "Start of a frame")
           (is (= nil (bow/bonus [2])) "First roll with no strike")
           (is (= nil (bow/bonus [1 2])) "Complete frame with no bonus")
           (is (= :spare (bow/bonus [8 2])) "Complete frame with spare")
           (is (= :strike (bow/bonus [10])) "Complete frame with strike")
           (is (= :strike (bow/bonus [10 10])) "Uncomplete 10nth frame with double strike")
           (is (= :strike (bow/bonus [10 10 7])) "Complete 10nth frame")
           (is (= :spare  (bow/bonus [8 2 7])) "Compelete 10nth frame with spare")))


(defcard-doc "## Compute the score
 The scoring of a frame is special because in case of a bonus you must wait for the result of the next roll(s) :


 - **Strike** : When all ten pins are knocked down with the first ball, a player is awarded ten points,
 plus a bonus of whatever is scored with the **next two balls**.
 - **spare** : When no pins are left standing after the second ball of a frame, a player is awarded ten points, plus a bonus of whatever is scored with **the next ball**


 In both cases, to score a frame, we have to take in account at most **3 rolls**.
 This is the purpose of the function `frame-score` : it takes a vector of three rolls. (possibly coming from the next frame(s)
 but for the time being this detail is out of concern.)"
             (mkdn-pprint-source bow/frame-score))


(deftest test-score-per-frame
         "### Let's put `frame-score`to the test"
         (testing "In order to compute an on-going game I have to score a frame in isolation and handle various cases. "
           (is (= 0 (bow/frame-score [])) "Frame with no roll")
           (is (= 7 (bow/frame-score [7])) "Frame with one roll")
           (is (= 9 (bow/frame-score [7 2])) "Frame with two rolls")
           (is (= 10 (bow/frame-score [10])) "Frame with one strike")
           (is (= 10 (bow/frame-score [8 2])) "Frame with one spare")
           (is (= 20 (bow/frame-score [9 1 10])) "10th Frame with 3 rolls")))


(defcard-doc "## Now that `frame-score` is correct, let's feed the function with all the rolls it needs


How many frames do we need in order to compute the score of one frame ?


In the case of a **strike**, with need the **next two rolls**, and if the next two rolls are also strikes we possibly need the next two frames.
So the rolls of the frame-score function need the rolls of the current frame plus next 2 frames. So we must group the frames by 3.


The partition functions are a perfect fit for this job :

- We will use `partition-all` because obviously we do not want to drop frames ;
- we deduced we have to group frames by 3 ;
- and have a `step` of 1 because we want the next two frames of each frame.
```clojure
(partition-all 3 1 [[8 1] [9 1] [10] [10] [8 1] [7 2] [10] [10] [10] [8 2 9]])
```
=>"


             (partition-all 3 1 [[8 1] [9 1] [10] [10] [8 1] [7 2] [10] [10] [10] [8 2 9]])

             "Not bad for just one function available in clojure.core !

             We have still several minor steps and we are good :

             - flatten the result of each partition
             - apply frame-score on each
             - accumalate the scores
             "
             "Starting form the last result, let's see each step :

             - `(map flatten)` =>"


             (->> [[8 1] [9 1] [10] [10] [8 1] [7 2] [10] [10] [10] [8 2 9]]
                  (partition-all 3 1)
                  (map flatten))

             "- `(map frame-score)` =>"

             (->> [[8 1] [9 1] [10] [10] [8 1] [7 2] [10] [10] [10] [8 2 9]]
                  (partition-all 3 1)
                  (map flatten)
                  (map #(take 3 %))
                  (map bow/frame-score))

             "- We have the score for each frame but the board shows that score is accumulating, this is a job for reduce.
             But reduce will give us the final score, we need the intermediate steps, that's exactly what reductions does."

             "The complete scoring function  : "
             (mkdn-pprint-source bow/scores)
             (bow/scores [[8 1] [9 1] [10] [10] [8 1] [7 2] [10] [10] [10] [8 2 9]])
             )


(deftest test-scores
         (testing "Scoring of all frames - Scores are cumulatives"
           (is (= [9] (bow/scores [[8 1]])) "Score of starting game")
           (is (= (bow/scores [[8 1] [] [] [] [] [] [] [] [] []]) [9 9 9 9 9 9 9 9 9 9]) "Score of starting game with reductions")
           (is (= [9 19] (bow/scores [[8 1] [9 1]])) "Ongoing match")
           (is (= [9 29 39] (bow/scores [[8 1] [9 1] [10]])))
           (is (= [14 19 24] (bow/scores [[8 2] [4 1] [4 1]])) "First frame is a spare")
           (is (= [15 20 25] (bow/scores [[10] [4 1] [4 1]])) "First frame is a strike")))

(defcard-doc "# The UI !
The computation of the score was fun but it is only one aspect of a project.
My idea was to pursue this Kata with a visual simulation because it gives a dynamic aspect of the computation.

My addition to the initial Kata :

 - Give a visual representation of a Bowling Score board
 - Simulate the progression of a game")

(defcard-doc "## The power of unit visualization
It is hard to understand the power of Devcards if you don't try it for yourself.
One of the killer feature is to be able to visualize components in different states and in isolation.
It is a first step towards a visualisation test. (The validation is visual.)


Before Devcards, I used to start with the big picture, using the whole state. but now I concentrate on smaller pieces one at a time.
")

(defcard-doc "### The Frame component

My first UI task was to represent a single Frame. Even if it begs to use table, I was confident that I could use Flexbox with a minimum amount of tags and style.")

(defcard frame-without-bonus
         "Frame complete without bonus"
         (let [frame {:rolls [8 1]
                      :id 3
                      :score 19}]
           (ui/frame-view frame)))

(defcard-doc "Now that I am pleased with the look, I had to display the different states of a frame :

- empty
- ongoing
- strike
- spare
- tenth frame...

As the UI is a funciton of the state, I just have to write a function that transform the state to a ui state.")

(deftest test-frame-view
         (testing "The representation of a frame"
           (is (= (ui/display-empty []) ["_" "_"]))
           (is (= (ui/display-empty [1]) [1 "_"]))
           (is (= (ui/display-empty [1 2]) [1 2]))
           (is (= (ui/display-empty [10 10 10]) [10 10 10]))
           (is (= (ui/display-empty ["X" ""]) ["X" ""]))
           (is (= (ui/display-empty ["8" "/"]) ["8" "/"]))))


(deftest test-frame-bonus
         (testing "Correct display of frame with bonus"
           (is (= (ui/display-bonus []) []) "Leave empty frame intact")
           (is (= (ui/display-bonus [9]) [9]) "Leave one roll no bonus intact")
           (is (= (ui/display-bonus [8 1]) [8 1]) "Leave intact frame with no bonus")
           (is (= (ui/display-bonus [8 2]) [8 "/"]) "Spare")
           (is (= (ui/display-bonus [0 10]) [0 "/"]) "Spare not strike")
           (is (= (ui/display-bonus [8 2 8]) [8 "/" 8]) "Spare tenth frame")
           (is (= (ui/display-bonus [8 2 10]) [8 "/" "X"]) "Spare strike tenth frame")
           (is (= (ui/display-bonus [10]) ["X" ""]) "Strike")
           (is (= (ui/display-bonus [10 10 10]) ["X" "X" "X"]))))



(defcard frame-one-roll
         "Frame with one roll"
         (let [frame {:id 1
                      :rolls [5]
                      :score 5}]
           (ui/frame-view frame)))


(defcard frame-with-spare
         "Frame with spare bonus"
         (let [frame {:id 1
                      :rolls [8 2]
                      :score 10}]
           (ui/frame-view frame)))

(defcard frame-with-strike
         "Frame with strike bonus"
         (let [frame {:rolls [10]
                      :id 2
                      :score 29}]
           (ui/frame-view frame)))


(defcard tenth-frame
         "Tenth frame with spare and Extra ball"
         (let [frame {:rolls [8 2 10]
                      :id 10
                      :score 19}]
           (ui/frame-view frame)))

(defcard tenth-frame-only-strikes
         "FIX ME ! Tenth frame with strike and Extra ball"
         (let [frame {:rolls [10 10 10]
                      :id 10
                      :score 19}]
           (ui/frame-view frame)))


(defcard-doc
  "## The dynamic aspect of the scoring


  In the first two parts, we focused on static aspects of the Kata. It is time to move things e little bit.
  Now that the scoring and the display is done, the last part is to consider the progression of the scoring.
  ")



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



(defcard bowling-kata-simulation
         "## Finally "
         (dom-node
           (fn [_ node]
             (om/add-root! ui/reconciler ui/Game node))))