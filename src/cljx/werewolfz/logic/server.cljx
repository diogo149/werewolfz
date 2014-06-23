(ns werewolfz.logic.server
  (:require [werewolfz.utils.logging :as log]
            [werewolfz.logic.game :as game]
            [werewolfz.logic.state :as state]))

(declare close-websocket)

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
        :chsk/uidport-close (close-websocket {:uid uid
                                              :send-fn send-fn
                                              :session session})
        :chsk/ws-ping nil
        (server-handler (merge event-msg {:send-fn send-fn
                                          :id id
                                          :data data
                                          :uid uid
                                          :session session}))))))
;; --------------
;; misc functions
;; --------------

(defn sync-room-content
  [send-fn room-id]
  (let [uids (state/room->uids room-id)
        login-names (map state/uid->login uids)] ;; OPTIMIZE
    (doseq [room-uid uids]
      (send-fn room-uid [:rooms/content {:login-names login-names}]))))

(defn close-websocket
  [{:keys [uid send-fn]}]
  ;; TODO maybe clear the `login-name`?
  ;; disconnect from any rooms
  (when-let [room-id (state/uid->room uid)]
    (state/leave-room uid)
    (sync-room-content send-fn room-id)))

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
  (println "loading rooms!")
  (if-let [room-id (state/uid->room uid)]
    ;; want to behave the same as a join
    (do (send-fn uid [:rooms/found {:room-id room-id}])
        (sync-room-content send-fn room-id))
    (send-fn uid [:rooms/not-found])))

(defmethod server-handler :rooms/join
  [{:keys [uid data send-fn]}]
  (let [{:keys [room-id]} data]
    (state/join-room uid room-id)
    (send-fn uid [:rooms/found {:room-id room-id}])
    (sync-room-content send-fn room-id)))

(defmethod server-handler :rooms/leave
  [{:keys [uid data send-fn]}]
  (let [room-id (state/uid->room uid)]
    (state/leave-room uid)
    (send-fn uid [:rooms/not-found])
    (sync-room-content send-fn room-id)))

(defmethod server-handler :rooms/chat
  [{:keys [data uid send-fn]}]
  (let [{:keys [text]} data
        room-id (state/uid->room uid)
        uids (state/room->uids room-id)]
    (doseq [room-uid uids]
      (send-fn room-uid [:rooms/chat {:text text
                                      :sender (state/uid->login uid)}]))))

(defmethod server-handler :rooms/start
  [{:keys [uid data send-fn]}]
  (let [{:keys [room-id]} data]
    (let [uids (state/room->uids room-id) ;; TODO dedupe
          login-names (map state/uid->login uids)
          game-state (game/basic-setup (count login-names) login-names)]
      (state/set-game room-id game-state)
      (doseq [room-uid uids]
        (send-fn room-uid
                 [:game/start
                  {:start-role (-> game-state
                                   :starting-roles
                                   ;; OPTIMIZE
                                   (get (state/uid->login room-uid)))}])))))

(defmethod server-handler :game/choice
  [{:keys [data uid send-fn]}]
  ;; TODO: After the choice is made, try running game until you can't anymore or it
  ;; is daytime.
  (state/swap-game (state/uid->room uid)
                   #(game/add-input % (state/uid->login uid) data)))
