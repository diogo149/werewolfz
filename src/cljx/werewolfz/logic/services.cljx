(ns werewolfz.logic.services
  (:require #+cljs [werewolfz.websocket :as ws]
            [werewolfz.utils.logging :as log]
            [werewolfz.logic.state :as state]))

(def ^:dynamic *send!*
  #+cljs ws/send!
  #+clj (fn [& args] (log/info "Sending on ws:" args)))

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
