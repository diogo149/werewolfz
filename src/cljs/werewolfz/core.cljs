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

(defn login-component
  []
  (let [login-name (reagent/atom "")]
    (fn []
      [:div [:h3 "Enter login name"]
       [:input {:type "text"
                :value @login-name
                :on-change #(reset! login-name (-> % .-target .-value))
                :on-key-up  (fn [e] (when (= 13 (.-which e))
                                      (srv/set-login @login-name)
                                      (reset! login-name "")))}]])))

(defn chatroom-component
  []
  (if (state/get-current-chatroom)
    [chat-component]
    [:div [:h2 "Chatrooms:" (pr-str (state/get-login-name))]
     (into [:ul]
           (for [chatroom ["cleo" "nixon" "angel"]]
             [:li {:on-click #(srv/join-chat chatroom)} chatroom]))]))

(defn root
  []
  [:div [:h1 "Hello " (or (state/get-login-name) "stranger")]
   (case (state/get-login-state)
     :? (do (srv/load-login)
            [:div "Loading..."])
     :failure [login-component]
     :success [chatroom-component])])

(reagent/render-component [root] (.getElementById js/document "content"))
