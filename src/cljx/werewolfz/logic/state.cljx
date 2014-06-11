(ns werewolfz.logic.state
  (:require #+cljs [reagent.core :as reagent]
            [werewolfz.utils.logging :as log]))

(def ratom #+cljs reagent/atom #+clj atom)

;; ----
;; chat
;; ----

(def chat-ratom (ratom nil))
(def current-chatroom-ratom (ratom nil))

(defn get-chat
  []
  (reverse @chat-ratom))

(defn conj-chat
  [chat]
  (swap! chat-ratom #(->> % (cons chat) (take 5))))

(defn clear-chat
  []
  (reset! chat-ratom nil))

(defn get-current-chatroom
  []
  @current-chatroom-ratom)

(defn set-current-chatroom
  [chatroom]
  (reset! current-chatroom-ratom chatroom))

;; ---------
;; chatrooms
;; ---------

(def chatrooms-ratom (ratom {}))

(defn get-chatroom
  [room-id]
  (keys (get @chatrooms-ratom room-id)))

(defn set-chatroom
  [room-id user-ids]
  (swap! chatrooms-ratom assoc-in [room-id] (zipmap user-ids (repeat true))))

(defn subscribe-chatroom
  [user-id room-id]
  (swap! chatrooms-ratom assoc-in [room-id user-id] true))

(defn unsubscribe-chatroom
  [user-id room-id]
  (swap! chatrooms-ratom update-in [room-id] #(dissoc % user-id)))
