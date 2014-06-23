(ns werewolfz.logic.state
  (:require #+cljs [reagent.core :as reagent]
            [werewolfz.utils.logging :as log]))

(def ratom #+cljs reagent/atom #+clj atom)

;; -----------------
;; connection status
;; -----------------

(def connected?-ratom (ratom false))
(def send-buffer-ratom (ratom []))

(defn connected?
  []
  @connected?-ratom)

(defn set-connected?
  [b]
  (reset! connected?-ratom b))

(defn send-buffer-msg!
  [msg]
  (swap! send-buffer-ratom conj msg))

(defn flush-send-buffer!
  [send-fn]
  (let [contents (atom nil)]
    (swap! send-buffer-ratom
           (fn [b]
             (reset! contents b)
             []))
    (doseq [msg @contents]
      (send-fn msg))))

;; ------
;; logins
;; ------

(def login-name-ratom (ratom nil))
(def login-state-ratom (ratom :?))

(defn get-login-name
  []
  @login-name-ratom)

(defn set-login-name
  [login-name]
  (reset! login-name-ratom login-name))

(defn get-login-state
  []
  @login-state-ratom)

(defn set-login-state
  [state]
  (reset! login-state-ratom state))

(def logins-ratom (ratom {}))

(defn set-login
  [uid login-name]
  (let [err (atom nil)]
    (swap! logins-ratom
           (fn [logins]
             (if (contains? logins login-name)
               (do (reset! err :taken)
                   logins)
               (assoc logins login-name uid))))
    @err))

(defn uid->login
  [uid]
  (->> @logins-ratom
       (filter (comp #(= uid %) second))
       first
       first))

(defn login->uid
  [login]
  (get @logins-ratom login))

;; -----
;; rooms
;; -----

(def uid->room-ratom (ratom nil))
(def room-state-ratom (ratom :?))
(def room-content-ratom (ratom nil))

(defn get-rooms
  []
  (map str (range 3)))

(defn uid->room
  [uid]
  (get @uid->room-ratom uid))

(defn room->uids
  [room-id]
  (->> @uid->room-ratom
       (filter (comp #(= % room-id) second))
       (map first)))

(defn join-room
  [uid room-id]
  (swap! uid->room-ratom assoc uid room-id))

(defn leave-room
  [uid]
  (swap! uid->room-ratom dissoc uid))

(defn get-room-state
  []
  @room-state-ratom)

(defn set-room-state
  [state]
  (reset! room-state-ratom state))

(defn get-room-content
  []
  @room-content-ratom)

(defn set-room-content
  [login-names]
  (reset! room-content-ratom login-names))







(def rooms-ratom (ratom nil))
(defn get-room
  [room-id]
  (get @rooms-ratom room-id))
(defn set-room
  [room-id val]
  (swap! rooms-ratom assoc room-id val))

;; ----
;; game
;; ----

(def games-ratom (ratom nil))

(defn set-game
  [room-id game-state]
  (swap! games-ratom assoc room-id game-state))

(defn get-game
  [room-id]
  (get @games-ratom room-id))

;; ----
;; chat
;; ----

(def chat-ratom (ratom []))
(def current-chatroom-ratom (ratom nil))

(defn get-chat
  []
  (reverse @chat-ratom))

(defn conj-chat
  [chat]
  (swap! chat-ratom conj chat))

(defn clear-chat
  []
  (reset! chat-ratom []))
