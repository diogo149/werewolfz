(ns werewolfz.core
  (:require [reagent.core :as reagent]
            [werewolfz.utils.time :as timeu]
            [werewolfz.logic.state :as state]
            [werewolfz.logic.services :as srv]
            [werewolfz.logic.game :as game]
            [werewolfz.game :as game-view]))

(defn chat-component
  []
  (let [chat (reagent/atom "")]
    (fn []
      [:div
       [:input {:type "text"
                :value @chat
                :on-change #(reset! chat (-> % .-target .-value))
                :on-key-up  (fn [e] (when (= 13 (.-which e))
                                      (srv/new-chat @chat)
                                      (reset! chat "")))}]
       (into [:ul]
             (for [[sender text date] (state/get-chat)]
               [:li [:b sender ": "]
                "(" (timeu/date->hh:MM:ss date) ") "
                text]))])))

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
  [:div [:h2 "Rooms:"]
   (into [:ul]
         (for [room-id (state/get-rooms)]
           [:li {:on-click #(srv/join-room room-id)} room-id]))])

(defn room-component
  []
  (let [room-id (state/get-room-state)
        login-names (state/get-room-content)]
    [:div
     "People in room:" (pr-str login-names)
     [:br]
     (if (state/get-in-game?)
       ;; show game
       [game-view/game-component]
       ;; show room info if not in game
       [:div
        [:button {:on-click srv/leave-room} "Leave Room"]
        (when (<= 3 (count login-names))
          [:button {:on-click #(srv/start-game room-id)} "Start!"])
        [:br]
        "In room: " room-id
        [:br]])
     [:br]
     [:h3 "Chat"]
     [chat-component]]))

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
      [:div
       [:h1 "Hi " (or (state/get-login-name) "stranger")]
       [:h3 (state/get-temporary-message)]
       (case (state/get-login-state)
         :? [:div "Loading..."]
         :failure [login-component]
         :success [logged-in-component])])}))

(reagent/render-component [root] (.getElementById js/document "content"))
