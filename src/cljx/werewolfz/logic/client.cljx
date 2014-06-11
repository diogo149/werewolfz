(ns werewolfz.logic.client
  (:require [werewolfz.utils.logging :as log]
            [werewolfz.logic.state :as state]))

(defmulti client-handler
  "Methods for handling messages for the client"
  :id)

(defmethod client-handler :default
  [{:keys [id data]}]
  (log/error "Unmatched client event:" [id data]))

(defn handler
  [{:keys [send-fn]}]
  (fn [[kw payload :as event]]
    (log/trace "Received event:" event)
    (case kw
      ;; sente basic messages
      :chsk/state nil ;; TODO handle connection event
      :chsk/timeout (log/error "Timeout occured for websocket.")
      :chsk/recv (let [[id data] payload]
                   (client-handler {:send-fn send-fn
                                    :id id
                                    :data data}))
      (log/error "Unmatched sente event:" event))))

;; ---------------
;; handler methods
;; ---------------

(defmethod client-handler :chat/msg
  [{:keys [data]}]
  (let [{:keys [text room-id]} data]
    (when (= room-id (state/get-current-chatroom))
      (state/conj-chat text))))

(defmethod client-handler :chat/room
  [{:keys [data]}]
  (let [{:keys [room-id users]} data]
    (println "USERS" users)
    (state/set-chatroom room-id users)))
