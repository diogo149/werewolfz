(ns werewolfz.logic.server
  (:require [werewolfz.utils.logging :as log]
            [werewolfz.logic.state :as state]))

(defmulti server-handler
  "Methods for handling messages for the server"
  :id)

(defmethod server-handler :default
  [{:keys [event]}]
  (log/error "Unmatched event:" event))

(defn handler
  [{:keys [send-fn]}]
  (fn [{:keys [ring-req event ?reply-fn] :as event-msg}]
    (let [session (:session ring-req)
          uid (:uid session)
          [id data] event]
      (log/trace "Received event:" event)
      (case id
        :chsk/uidport-open (log/trace "Opening websocket")
        :chsk/uidport-close (log/trace "Closing websocket")
        :chsk/ws-ping nil
        (server-handler (merge event-msg {:send-fn send-fn
                                          :id id
                                          :data data
                                          :uid uid
                                          :session session}))))))

;; ---------------
;; handler methods
;; ---------------

(defmethod server-handler :login/load
  [{:keys [uid data send-fn]}]
  (send-fn uid
           (if-let [login-name (state/uid->login uid)]
             [:login/success {:login-name login-name}]
             [:login/failure {:error :not-found}])))

(defmethod server-handler :login/set
  [{:keys [uid data send-fn]}]
  (let [{:keys [login-name]} data]
    (log/trace "Trying to login as" login-name)
    (send-fn uid
             (if-let [err (state/set-login uid login-name)]
               [:login/failure {:error err}]
               [:login/success {:login-name login-name}]))))

(defmethod server-handler :rooms/load
  [{:keys [uid data send-fn] :as msg}]
  (if-let [room-id (state/uid->room uid)]
    ;; want to behave the same as a join
    (server-handler (assoc msg
                      :id :rooms/join
                      :data {:room-id room-id}))
    (send-fn uid [:rooms/not-found])))

(defmethod server-handler :rooms/join
  [{:keys [uid data send-fn]}]
  (let [{:keys [room-id]} data]
    (state/join-room uid room-id)
    (send-fn uid [:rooms/found {:room-id room-id}])
    (let [uids (state/room->uids room-id)
          login-names (map state/uid->login uids)] ;; OPTIMIZE
      (doseq [room-uid uids]
        (send-fn room-uid [:rooms/content {:login-names login-names}])))))

(defmethod server-handler :rooms/leave
  [{:keys [uid data send-fn]}]
  (state/leave-room uid)
  (send-fn uid [:rooms/not-found]))





(defmethod server-handler :chat/join
  [{:keys [uid data send-fn]}]
  (let [{:keys [room-id]} data]
    (when room-id
      (state/subscribe-chatroom uid room-id)
      (let [chatroom (state/get-chatroom room-id)]
        (doseq [user-id chatroom]
          (send-fn user-id [:chat/room {:room-id room-id
                                        :users chatroom}]))))))

(defmethod server-handler :chat/msg
  [{:keys [data uid send-fn]}]
  (let [{:keys [text room-id]} data
        chatroom (state/get-chatroom room-id)]
    (doseq [user-id chatroom]
      (send-fn user-id [:chat/msg {:text text
                                   :room-id room-id}]))))
