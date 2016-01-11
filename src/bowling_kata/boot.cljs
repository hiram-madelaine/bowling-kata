(ns bowling-kata.boot
  (:require [bowling-kata.core :as bow]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [clojure.string :as str]
            [cljs.pprint :refer [pprint] :as pp]))

(enable-console-print!)

(def state (bow/->game [[8 1] [9 1] [10] [10] [8 1] [7 2] [10] [10] [10] [8 2 9]]))

(prn (bow/game-score state))



;________________________________________________
;                                                |
;         Component                              |
;________________________________________________|



(defui Frame
  Object
  (render [this]
    (let [rolls (:rolls (om/props this))
          bonus (bow/bonus rolls)]
      (apply dom/div #js {:className "rolls"}
             (for [roll rolls]
               (dom/div #js {:className "roll"} roll))))))

(def frame (om/factory Frame))

(defui Game
  Object
  (render [this]
    (let [game (om/props this)]
      (dom/div #js {}
                 (apply dom/div #js {:className "game"} (map frame game))))))

(def game (om/factory Game))

;________________________________________________
;                                                |
;         Parser                                 |
;________________________________________________|





;________________________________________________
;                                                |
;         Reconciler                             |
;________________________________________________|


#_(def reconciler (om/reconciler {:state game}))


#_(om/add-root! reconciler Game (gdom/$ "app") )


(js/ReactDOM.render (game state) (gdom/getElement "app"))