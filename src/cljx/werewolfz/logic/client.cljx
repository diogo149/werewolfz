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
      :not-found (state/set-temporary-message "Login not found")
      :taken (state/set-temporary-message "Login already taken"))))

(defmethod client-handler :rooms/list
  [{:keys [data]}]
  (let [{:keys [room-ids]} data]
    (state/set-rooms room-ids)))

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

(defmethod client-handler :rooms/chat
  [{:keys [data]}]
  (let [{:keys [text sender]} data]
    (state/conj-chat [sender text
                      #+clj (java.util.Date.) #+cljs (js/Date.)])))

(defmethod client-handler :game/start
  [{:keys [data]}]
  (let [{:keys [start-role]} data]
    ;; TODO something
    (state/set-in-game? true)
    (state/set-starting-role start-role)))

(defmethod client-handler :game/daytime
  [{:keys [data]}]
  (let [{:keys [output]} data]
    (state/set-output output)))
