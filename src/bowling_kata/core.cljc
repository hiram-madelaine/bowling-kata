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

(s/defschema Rolls
  "Three rolls"
  [(s/one Roll "First Roll")
   (s/optional Roll "Second Roll")
   (s/optional Roll "Extra Ball")])

(s/defschema Frame Rolls)

(s/defschema Game [Frame])

;________________________________________________
;                                                |
;         API                                    |
;________________________________________________|

(def add
  "Special + that handle nil parameters."
  (fnil + 0 0 0))

(s/defn bonus :- (s/maybe Bonus)
  "Determine if frame has a bonus"
  [rolls :- Rolls]
  (let [[roll1 roll2] rolls]
    (match [roll1 roll2 (add roll1 roll2)]
           [10 _ _] :strike
           [_ (_ :guard pos?) 10] :spare
           :else nil)))

(s/defn frame-score :- Score
  "The frame's score."
  [rolls :- [Roll]]
  (let [[r1 r2 r3] rolls]
    (match [(bonus rolls)]
           [(:or :spare :strike)] (add r1 r2 r3)
           :else (add r1 r2))))

(def x-score
  "Transducer for "
  (comp (map flatten)
        (map #(take 3 %))
        (map frame-score)))


(s/defn game-score :- [Score]
  "Cumulative score for all frames"
  [game :- Game]
  (->> game
       (partition-all 3 1) ; This arity can not ne included in the transducer
       (into [] x-score)
       (reductions +)))

(s/defn total-score :- Score
  [game :- Game]
  (->> game
       (partition-all 3 1)
       (transduce x-score +)))


