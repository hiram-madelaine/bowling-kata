(ns bowling-kata.boot
  (:require [bowling-kata.core :as bow]))

(enable-console-print!)


(prn (bow/game-score [[8 1] [9 1] [10] [10] [8 1] [7 2] [10] [10] [10] [8 2 9]]))