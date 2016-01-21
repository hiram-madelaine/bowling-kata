(ns bowling-kata.core
  (:require [schema.core :as s :include-macros true]
            [com.rpl.specter :as spec :refer [transform select FIRST LAST ALL comp-paths collect select-one setval putval] :include-macros true]
            #?(:clj [clojure.core.match :refer [match]]
               :cljs [cljs.core.match :refer-macros [match]])))

;________________________________________________
;                                                |
;         Schemas                                |
;________________________________________________|

(s/defschema Roll
  "The number of pin's knock down for a roll."
  (s/constrained s/Int #(<= 0 % 10) "At most, ten pins can be knocked down."))

(s/defschema Score
  (s/constrained s/Int #(<= 0 % 300) "From zero pin knocked down to the perfect game with 12 strikes."))

(s/defschema Bonus
  "A frame may have a bonus"
  (s/maybe (s/enum :spare :strike)))

(s/defschema FrameRolls
  "In a frame there can be from 1 to 3 rolls.
  One Roll (strike) to three Rolls on the 10nth Frame"
  [(s/one Roll "First Roll")
   (s/optional Roll "Second Roll")
   (s/optional Roll "Extra Ball")])


(s/defschema GameRolls "All rolls of the entire Game" [FrameRolls])

;________________________________________________
;                                                |
;         Score API                              |
;________________________________________________|

(def add
  "Special + that handle nil parameters."
  (fnil + 0 0 0))

(def pins-down?
  "At least one pin down"
  (fnil pos? 0))

(s/defn bonus :- Bonus
  "Determine the bonus of any frame."
  [[roll1 roll2] :- FrameRolls]
  (match [roll1 roll2 (add roll1 roll2)]
         [10 _ _] :strike
         [_ (_ :guard pins-down?) 10] :spare
         :else nil))

(s/defn frame-score :- Score
  "The frame's score."
  [rolls :- [Roll]]
  (let [[r1 r2 r3] rolls]
    (if (bonus rolls)
      (add r1 r2 r3)
      (add r1 r2))))

(s/defn scores :- [Score]
  "Cumulative score for all frames"
  [all-rolls :- GameRolls]
  (->> all-rolls
       (partition-all 3 1)
       (map flatten)
       (map frame-score)
       (reductions +)))


(s/defn score :- Score
  [all-rolls :- GameRolls]
  (last (reductions + (scores all-rolls))))

;________________________________________________
;                                                |
;         UI API                                 |
;________________________________________________|

(s/defschema Frame {:rolls FrameRolls
                    :bonus Bonus
                    :id    s/Int
                    :score Score})

(s/defschema Game {:frames                        [Frame]
                   (s/optional-key :round) s/Int})


(s/defn frame-done?
  "2 rolls in the 9th first Frames
   2 rolls in the 10th Frame if no bonus."
  [{:keys [rolls id]} :- Frame]
  (match [(bonus rolls) id (count rolls)]
         [(:or :strike :spare) (_ :guard #(< % 10)) _] true
         [(:or :strike :spare) 10 3] true
         [nil _ 2] true
         :else false))

(s/defn all-rolls :- GameRolls
  [game :- Game]
  (->> game
       :frames
       (mapv :rolls)))

(s/defn update-score :- Game
  "Update the game with the score for past and current frame.
   TODO use Specter to update all frames up to the current."
  [{:keys [round frames] :as game} :- Game]
  (let [all-scores (->> game all-rolls scores)
        frames (map-indexed (fn [i f]
                              (if (<= i (dec round))
                                (assoc f :score (nth all-scores i))
                                f)) frames)]
    {:round  round
     :frames frames}))