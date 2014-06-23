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

(defn rooms-component
  []
  (if (state/get-current-chatroom)
    [chat-component]
    [:div [:h2 "Rooms:"]
     (into [:ul]
           (for [room-id (state/get-rooms)]
             [:li {:on-click #(srv/join-room room-id)} room-id]))]))

(defn room-component
  []
  [:div [:button {:on-click srv/leave-room} "Leave Room"]
   [:br]
   "In room: " (state/get-room-state)
   [:br]
   "People in room:" (pr-str (state/get-room-content))])

(defn logged-in-component
  []
  (reagent/create-class
   {:component-will-mount #(srv/load-room)
    :render
    (fn [this]
      (case (state/get-room-state)
        :? [:div "Loading..."]
        :not-found [rooms-component]
        [room-component]))}))

(defn root
  []
  (reagent/create-class
   {:component-will-mount #(srv/load-login)
    :render
    (fn [this]
      [:div [:h1 "Hello " (or (state/get-login-name) "stranger")]
       (case (state/get-login-state)
         :? [:div "Loading..."]
         :failure [login-component]
         :success [logged-in-component])])}))

(reagent/render-component [root] (.getElementById js/document "content"))
