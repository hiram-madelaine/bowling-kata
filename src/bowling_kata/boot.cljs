(ns bowling-kata.boot
  (:require [bowling-kata.core :as bow]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [clojure.string :as str]
            [cljs.pprint :refer [pprint] :as pp]
            [com.rpl.specter :as spec :refer [transform select FIRST LAST ALL comp-paths select-one] :include-macros true]))

(enable-console-print!)

(def empty-game (merge {:round 1} (bow/->game (repeat 10 []))))
(def state empty-game
  #_(bow/->game [[8 1] [9 1] [10] [10] [8 1] [7 2] [10] [10] [10] [8 2 9]]))



(defn second-roll []
  (let [r1 (rand-int 10)
       max (- 10 r1)]
   (prn [r1 (rand-int max)])))

;________________________________________________
;                                                |
;         Component                              |
;________________________________________________|

(defn display-rolls
  [rolls]
  (let [[r1 r2 r3] rolls]
    (condp = (bow/bonus rolls)
      :spare (if r3 [r1 "/" r3] [r1 "/"])
      :strike ["X" ""]
      rolls)))


(defui Frame
  static om/Ident
  (ident [this props]
    [:frame/by-id (:id props)])
  static om/IQuery
  (query [this]
    [:rolls :score :id])
  Object
  (render [this]
    (let [{:keys [id score rolls]} (om/props this)
          rolls (display-rolls rolls)]
      (dom/div #js {:className "frame"}
               (dom/div #js {:className "header frame-item"} id)
               (apply dom/div #js {:className "rolls"}
                             (for [roll rolls]
                               (dom/div #js {:className "frame-item"} roll)))
               (dom/div #js {:className "frame-item"}
                 (dom/div #js {:className ""} score))))))

(def frame (om/factory Frame))

(defui Game
  static om/IQuery
  (query [this]
    [{:frames (om/get-query Frame)} ])
  Object
  (render [this]
    (let [game (:frames (om/props this))]
      (dom/div #js{}
               (dom/button #js {:onClick (fn [e]
                                           (om/transact! this '[(game/roll) :frames]))} "Roll the Ball !")
               (dom/div #js {}
                (apply dom/div #js {:className "game"} (map frame game)))
               ))))


;________________________________________________
;                                                |
;         Parser Read                            |
;________________________________________________|

(defmulti read om/dispatch)

(defmethod read :frames
  [{:keys [state] :as env} key params]
  (let [st @state]
    (prn @state)
    {:value (get st key)}))

;________________________________________________
;                                                |
;         Parser Read                            |
;________________________________________________|

(defmulti mutate om/dispatch)



(defn next-game
  [{:keys [round] :as state}]
  (let [state (transform [:frames ALL #(= round (:id %))] #(update-in % [:rolls] conj (rand-int 10)) state)]
    (if (bow/frame-done? (select-one [:frames ALL #(= round (:id %))] state))
         (transform [:round] inc state)
         state)))

(defmethod mutate 'game/roll
  [{:keys [state] :as env} key params]
  {:action #(swap! state next-game)})

;________________________________________________
;                                                |
;         Reconciler                             |
;________________________________________________|


(def reconciler (om/reconciler {:parser (om/parser {:read read
                                                    :mutate mutate})
                                :state  state}))


(om/add-root! reconciler Game (gdom/$ "app"))


#_(js/ReactDOM.render (game state) (gdom/getElement "app"))