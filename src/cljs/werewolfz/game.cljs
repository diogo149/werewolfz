(ns werewolfz.game
  (:require [reagent.core :as reagent]
            [werewolfz.logic.state :as state]
            [werewolfz.logic.services :as srv]
            [werewolfz.logic.game :as game]))










(defn select-player-component
  [on-click-fn]
  [:div
   (for [username (remove #(= (state/get-login-name) %) (state/get-room-content))]
     [:button {:on-click #(on-click-fn username)} username])])










(defmulti choice-comp
 :choice-type)

(defmethod choice-comp :seer
  [_]
  [:div
   "Would you like to see a player's card, or two of the center cards?"
   [:button {:on-click #(srv/make-choice {:choice-type :seer
                                          :choice :person})}
    "Person's card"]
   [:button {:on-click #(srv/make-choice {:choice-type :seer
                                          :choice :middle})}
    "Two Middle Cards"]])

(defmethod choice-comp :seer-choose-person
  [_]
  [:div
   "Please choose a player's card to view!"
   [select-player-component #(srv/make-choice {:choice-type :seer-choose-person
                                               :person %})]])

(defmethod choice-comp :seer-choose-middle-first
  []
  [:div
   "Choose a middle card to view!"
   (for [index (range 3)]
     [:button {:on-click #(srv/make-choice {:choice-type :seer-choose-middle-first
                                            :middle-index index})}
      (str (+ 1 index))])])

(defmethod choice-comp :seer-choose-middle-second
  [{:keys [first-middle]}]
  [:div
   "Choose a second middle card to view!"
   (for [index (range 3)]
     (when (not= index first-middle)
       [:button {:on-click #(srv/make-choice {:choice-type :seer-choose-middle-second
                                              :first-middle first-middle
                                              :second-middle index})}
        (str (+ 1 index))]))])

(defmethod choice-comp :default
  [_]
  [:div "You don't have any decisions to make, but feel free to click
        this button if you're playing in person so your friends think
        you have an interesting role :)"
   [:button "Click me!!!"]])

(defmethod choice-comp :robber
  [_]
  [:div
   "Please choose a player's card to steal!"
   [select-player-component #(srv/make-choice {:choice-type :robber
                                               :person %})]])

(defn game-component
  []
  [:div
   "Starting role: " (when-let  [role (state/get-starting-role)]
                       (name role))
   (when (state/get-choice)
     [choice-comp (state/get-choice)]

     )
   (when-let [output (state/get-output)]
     [:div "Output:: " (pr-str output)])])
