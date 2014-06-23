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
  (*send!* [:rooms/chat {:text chat}]))

;; -------------
;; game services
;; -------------

(defmulti make-choice
  :choice-type)

;; TODO: state/set-choice to be called in game.cljs
;; services only for making the choice.
(defmethod make-choice :seer-first-choice
  [{:keys [choice]}]
  (if :middle
    (state/set-choice {:choice-type :seer-choose-middle-first})
    (state/set-choice {:choice-type :seer-choose-person})))

(defmethod make-choice :seer-choose-person
  [{:keys [person]}]
  (state/set-choice nil)
  (*send!* [:game/input {:input-type :seer
                         :seer-choice "person"
                         :seer-person-to-see person}]))
