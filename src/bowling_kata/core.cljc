(ns bowling-kata.core
  (:require [schema.core :as s :include-macros true]
            #?(:clj [clojure.core.match :refer [match]]
               :cljs [cljs.core.match :refer-macros [match]])))

;________________________________________________
;                                                |
;         Schemas                                |
;________________________________________________|

(s/defschema Roll s/Int)

(s/defschema Score s/Int)

(s/defschema Bonus (s/maybe (s/enum :spare :strike)))

(s/defschema FrameRolls
  "One Roll (strike) to three Rolls on the 10nth Frame"
  [(s/one Roll "First Roll")
   (s/optional Roll "Second Roll")
   (s/optional Roll "Extra Ball")])

(s/defschema GameRolls "All rolls of the entire Game" [FrameRolls])

(s/defschema Frame {:rolls FrameRolls
                    :bonus Bonus
                    :id s/Int
                    :score Score})

(s/defschema Game {:game [Frame]})

;________________________________________________
;                                                |
;         Score API                              |
;________________________________________________|

(def add
  "Special + that handle nil parameters."
  (fnil + 0 0 0))

(def positive?
  (fnil pos? -1))

(s/defn bonus :- Bonus
  "Determine if frame has a bonus"
  [rolls :- FrameRolls]
  (let [[roll1 roll2] rolls]
    (match [roll1 roll2 (add roll1 roll2)]
           [10 _ _] :strike
           [_ (_ :guard positive?) 10] :spare
           :else nil)))

(s/defn frame-score :- Score
  "The frame's score."
  [rolls :- [Roll]]
  (let [[r1 r2 r3] rolls]
    (match [(bonus rolls)]
           [(:or :spare :strike)] (add r1 r2 r3)
           :else (add r1 r2))))

(def x-score
  "Transducer to compute all frames score."
  (comp (map flatten)
        (map #(take 3 %))
        (map frame-score)))


(s/defn scores :- [Score]
  "Cumulative score for all frames"
  [all-rolls :- GameRolls]
  (->> all-rolls
       (partition-all 3 1) ; This arity can not ne included in the transducer
       (into [] x-score)
       (reductions +)))

(s/defn score :- Score
  [all-rolls :- GameRolls]
  (nth (scores all-rolls) 9))


;________________________________________________
;                                                |
;         UI API                                 |
;________________________________________________|

(s/defn frame-done?
  "2 rolls in the 9th first Frames
  2 rolls in the 10th Frame if no bonus."
  [{:keys [rolls id] :as frame} :- Frame]
  (match [(bonus rolls) id (count rolls)]
         [(:or :strike :spare) (_ :guard #(< % 10)) _] true
         [nil _ 2] true
         [:strike 10 2] true
         [:spare 10 3] true
         :else false))

(s/defn ->game :- Game
  [all-rolls :- GameRolls]
  (->> all-rolls
      (map-indexed
             (fn [i rolls]
               (assoc {} :rolls rolls
                         :id (inc i)
                         :bonus (bonus rolls))))
       (map (fn [score frame]
              (assoc frame :score score)) (scores all-rolls))
       (assoc {} :game)))

(s/defn ->all-rolls :- GameRolls
  [game :- Game]
  (->> game
      :game
      (mapv :rolls)))