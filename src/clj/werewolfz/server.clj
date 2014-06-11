(ns werewolfz.server
  (:require [com.stuartsierra.component :as component]
            [org.httpkit.server :as httpkit]
            ring.middleware.session
            ring.middleware.session.cookie
            ring.middleware.params
            ring.middleware.keyword-params
            ring.middleware.anti-forgery
            ring.middleware.json
            [werewolfz.routes :as routes]
            werewolfz.websocket
            [werewolfz.utils.logging :as log]
            [werewolfz.utils.server :as serveru]))

(defn start-server
  [{:keys [production? websocket]}]
  (httpkit/run-server
   (-> routes/app-routes
       ((fn [handler]
          (fn [req]
            (handler req))))
       serveru/wrap-unified-edn-api ;; must be first
       (werewolfz.websocket/wrap-handle-websocket-requests websocket)
       serveru/wrap-session-atom
       (serveru/wrap-production?-flag production?)
       (ring.middleware.anti-forgery/wrap-anti-forgery
        {:read-token (fn [req] (-> req :params :csrf-token))}) ;; should be inside wrap-session
       (ring.middleware.session/wrap-session
        {:store (ring.middleware.session.cookie/cookie-store
                 {:key "thisisafakekey42"})}) ;; FIXME take as parameter
       ring.middleware.keyword-params/wrap-keyword-params
       ring.middleware.json/wrap-json-params
       ring.middleware.params/wrap-params
       serveru/wrap-request-logging)
   {:port 9090
    :thread 8
    :worker-name-prefix "server-"}))

(defrecord ServerComponent [production? ;; boolean
                            websocket ;; websocket component
                            server] ;; callback to cancel started server
  component/Lifecycle
  (start [this]
    ;; In the 'start' method, a component may assume that its
    ;; dependencies are available and have already been started.
    (log/info "Starting server")
    (if server
      this
      (let [new-server (start-server {:production? production?
                                      :websocket websocket})]
        (assoc this :server new-server))))
  (stop [this]
    ;; Likewise, in the 'stop' method, a component may assume that its
    ;; dependencies will not be stopped until AFTER it is stopped.
    (log/info "Stopping server")
    (server)
    (dissoc this :server)))

(defn server-component
  [production?]
  (map->ServerComponent {:production? production?}))
