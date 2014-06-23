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
      :chsk/state (do (state/set-connected? (:open? payload))
                      (state/flush-send-buffer! send-fn))
      :chsk/timeout (log/error "Timeout occured for websocket.")
      :chsk/recv (let [[id data] payload]
                   (client-handler {:send-fn send-fn
                                    :id id
                                    :data data}))
      (log/error "Unmatched sente event:" event))))

;; ---------------
;; handler methods
;; ---------------

(defmethod client-handler :login/success
  [{:keys [data]}]
  (let [{:keys [login-name]} data]
    (println "LOGGED IN AS" login-name)
    (state/set-login-state :success)
    (state/set-login-name login-name)))

(defmethod client-handler :login/failure
  [{:keys [data]}]
  (let [{:keys [error]} data]
    (state/set-login-state :failure)
    (case error
      :not-found (println "LOGIN NOT FOUND") ;; TODO
      :taken (println "LOGIN TAKEN")))) ;; TODO

(defmethod client-handler :rooms/found
  [{:keys [data]}]
  (let [{:keys [room-id]} data]
    (state/set-room-state room-id)))

(defmethod client-handler :rooms/not-found
  [{:keys [data]}]
  (state/set-room-state :not-found))

(defmethod client-handler :rooms/content
  [{:keys [data]}]
  (let [{:keys [login-names]} data]
    (state/set-room-content login-names)))

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
