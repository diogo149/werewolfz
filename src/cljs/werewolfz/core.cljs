(ns werewolfz.core
  (:require [reagent.core :as reagent]
            [werewolfz.logic.state :as state]
            [werewolfz.logic.services :as srv]))

(defn chat-component
  []
  (let [chat (reagent/atom "")]
    (fn []
      [:div
       (into [:ul]
             (for [chat (state/get-chat)]
               [:li chat]))
       [:input {:type "text"
                :value @chat
                :on-change #(reset! chat (-> % .-target .-value))
                :on-key-up  (fn [e] (when (= 13 (.-which e))
                                     (srv/new-chat @chat)
                                     (reset! chat "")))}]])))

(defn chatroom-component
  []
  (if (state/get-current-chatroom)
    [chat-component]
    [:div [:h2 "Chatrooms:"]
     (into [:ul]
           (for [chatroom ["cleo" "nixon" "angel"]]
             [:li {:on-click #(srv/join-chat chatroom)} chatroom]))]))

(defn root
  []
  [:div [:h1 "Hello world"]
   [chatroom-component]])

(reagent/render-component [root] (.getElementById js/document "content"))
