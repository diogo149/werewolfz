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

(defmethod choice-comp :seer-first-choice
  [_]
  [:div
   "Would you like to see a player's card, or two of the center cards?"
   [:button {:on-click #(srv/make-choice {:choice-type :seer-first-choice
                                          :choice :person}) }
    "Person's card"]
   [:button {:on-click #(srv/make-choice {:choice-type :seer-first-choice
                                          :choice :middle}) }
    "Two Middle Cards"]])












(defn game-component
  []
  [:div
   "Starting role: " (state/get-starting-role)
   (when (state/get-choice)
     [choice-comp (state/get-choice)])])
