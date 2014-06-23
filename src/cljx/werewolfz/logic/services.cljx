(ns werewolfz.logic.services
  (:require #+cljs [werewolfz.websocket :as ws]
            [werewolfz.utils.logging :as log]
            [werewolfz.logic.state :as state]))

(def ^:dynamic *send!*
  #+cljs
  (fn [msg]
    (if (state/connected?)
      (ws/send! msg)
      (state/send-buffer-msg! msg)))
  #+clj (fn [& args] (log/info "Sending on ws:" args)))

;; -----
;; login
;; -----

(defn load-login
  []
  (*send!* [:login/load]))

(defn set-login
  [login-name]
  (*send!* [:login/set {:login-name login-name}]))

;; -----
;; rooms
;; -----

(defn load-room
  []
  (*send!* [:rooms/load]))

(defn join-room
  [room-id]
  (*send!* [:rooms/join {:room-id room-id}]))

(defn leave-room
  []
  (*send!* [:rooms/leave]))

(defn start-game
  [room-id]
  (*send!* [:rooms/start {:room-id room-id}]))

;; ----
;; chat
;; ----

(defn new-chat
  [chat]
  (*send!* [:chat/msg {:text chat
                       :room-id (state/get-current-chatroom)}]))

(defn join-chat
  [room-id]
  (state/set-current-chatroom room-id)
  (*send!* [:chat/join {:room-id room-id}]))
