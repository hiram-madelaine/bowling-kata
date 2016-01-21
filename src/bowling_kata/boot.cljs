(ns bowling-kata.boot
  (:require [bowling-kata.core :as bow]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [clojure.string :as str]
            [cljs.pprint :refer [pprint] :as pp]
            [com.rpl.specter :as spec :refer [transform select FIRST LAST ALL comp-paths select-one putval] :include-macros true]
            [cljs.core.match :refer-macros [match]]
            [schema.core :as s :include-macros true]))

(enable-console-print!)

(def empty-state (merge {:round  1
                        :frames (for [n (range 1 11)]
                                  {:rolls []
                                   :id    n
                                   :bonus nil
                                   :score 0})}))

(def state (atom empty-state))

(defn second-roll []
  (let [r1 (rand-int 10)
       max (- 10 r1)]
   (prn [r1 (rand-int max)])))

;________________________________________________
;                                                |
;         Component                              |
;________________________________________________|

(defn display-bonus
  [rolls]
  (let [rolls (->> rolls (mapv #(get {10 "X"} % %)))
        [r1 r2 r3] rolls]
    (condp = (bow/bonus rolls)
      :spare (if r3 [r1 "/" r3] [r1 "/"])
      rolls)))

(defn display-empty
  [rolls]
  (let [[r1] rolls]
    (condp = (count rolls)
     0 ["_" "_"]
     1 [r1 "_"]
     rolls)))

(s/defn frame-display
  [{:keys [id score rolls] :as frame}  :- bow/Frame]
  (let [[r1 r2 r3] rolls]
    (match [id (bow/bonus rolls) (count rolls)]
          [10 :strike _] ["X" ])))

(defui FrameView
  static om/Ident
  (ident [this props]
    [:frame/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:rolls :score :id])
  Object
  (render [this]
    (let [{:keys [id score rolls]} (om/props this)
          rolls (-> rolls display-bonus  display-empty)]
      (dom/div #js {:className "frame"}
               (dom/div #js {:className "header frame-item"} id)
               (apply dom/div #js {:className "rolls"}
                             (for [roll rolls]
                               (dom/div #js {:className "frame-item"} roll)))
               (dom/div #js {:className "frame-item"}
                 (dom/div #js {:className ""} score))))))

(def frame-view (om/factory FrameView))

(defui Game
  static om/IQuery
  (query [this]
    [{:frames (om/get-query FrameView)} ])
  Object
  (render [this]
    (let [game (:frames (om/props this))]
      (dom/div #js{}
               (dom/button #js {:onClick (fn [e]
                                           (om/transact! this '[(game/roll) :frames]))} "Roll the Ball !")
               (dom/button #js {:onClick (fn [e]
                                           (om/transact! this '[(game/reset) :frames]))} "Reset the game !")
               (dom/div #js {}
                        (apply dom/div #js {:className "game"} (map frame-view game)))))))


;________________________________________________
;                                                |
;         Parser Read                            |
;________________________________________________|

(defmulti read om/dispatch)

(defmethod read :frames
  [{:keys [state] :as env} key params]
  (let [st @state]
    {:value (get st key)}))

;________________________________________________
;                                                |
;         Parser Read                            |
;________________________________________________|

(defmulti mutate om/dispatch)

(defn next-roll
  "Simulate the next roll given the previous roll.
  Does not take into account the tenth frame."
  [rolls]
  (let [pins (apply + rolls)
        left (- 10 pins)
        next (rand-int (inc left))] ;too easy to do a spare when 9 or 8
    (conj rolls next)))

(s/defn update-round :- Game
  "Update the round if the frame is done."
  [{:keys [round] :as game} :- Game ]
  (prn game)
  (if (bow/frame-done? (select-one [:frames ALL #(= round (:id %))] game))
    (transform [:round] inc game)
    game))

(s/defn next-game :- bow/Game
  [{:keys [round] :as game} :- Game
   next-fn]
  (->> game
       (transform [:frames ALL #(= round (:id %))]
                  #(update-in % [:rolls] next-fn))
       bow/update-score
       update-round))

(defmethod mutate 'game/roll
  [{:keys [state] :as env} key params]
  (prn @state)
  {:action #(swap! state next-game next-roll)})

(defmethod mutate 'game/reset
  [{:keys [state] :as env} key params]
  {:action #(reset! state empty-state)})

;________________________________________________
;                                                |
;         Reconciler                             |
;________________________________________________|


(def reconciler (om/reconciler {:parser (om/parser {:read read
                                                    :mutate mutate})
                                :state  state}))


(om/add-root! reconciler Game (gdom/$ "app"))


#_(js/ReactDOM.render (game state) (gdom/getElement "app"))