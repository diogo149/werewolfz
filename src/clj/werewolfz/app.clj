(ns werewolfz.app
  (:require [com.stuartsierra.component :as component]
            werewolfz.logic.server
            werewolfz.websocket
            werewolfz.server))

(defn main-system
  [{:keys [production?]}]
  (component/system-map
   :websocket (werewolfz.websocket/websocket-component)
   :websocket-handler
   (component/using (werewolfz.websocket/websocket-handler-component
                     werewolfz.logic.server/handler)
                    [:websocket])
   :server (component/using (werewolfz.server/server-component production?)
                            [:websocket])))
