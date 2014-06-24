(ns werewolfz.app
  (:require [com.stuartsierra.component :as component]
            werewolfz.logic.server
            werewolfz.websocket
            werewolfz.timer
            werewolfz.server))

(defn get-port
  []
  (if-let [port (System/getenv "PORT")]
    (Integer/parseInt port)
    9090))

(defn main-system
  [{:keys [production?]}]
  (component/system-map
   :timer (werewolfz.timer/timer-component)
   :websocket (werewolfz.websocket/websocket-component)
   :websocket-handler
   (component/using (werewolfz.websocket/websocket-handler-component
                     werewolfz.logic.server/handler)
                    [:websocket])
   :server (component/using (werewolfz.server/server-component production?
                                                               (get-port))
                            [:websocket])))
