(ns werewolfz.websocket
  (:require [com.stuartsierra.component :as component]
            [werewolfz.utils.logging :as log]
            [werewolfz.utils.async :as asyncu]
            [taoensso.sente :as sente]))

(defn csrf-token-fn
  [ring-req]
  (get-in ring-req [:session :ring.middleware.anti-forgery/anti-forgery-token]))

(defrecord WebsocketComponent [ch-recv ;; ChannelSocket's receive channel
                               send-fn ;; ChannelSocket's send API fn
                               ajax-post-fn
                               ajax-get-or-ws-handshake-fn
                               connected-uids ;; Watchable, read-only atom
                               initialized?] ;; boolean
  component/Lifecycle
  (start [this]
    (if initialized?
      this
      (do (log/info "Creating websocket channel")
          (let [socket-map (sente/make-channel-socket!
                            {:csrf-token-fn csrf-token-fn})]
            (assoc (merge this socket-map) :initialized? true)))))
  (stop [this]
    (log/info "Closing websocket channel")
    this))

(defn websocket-component
  [& args]
  (map->WebsocketComponent {:initialized? false}))

(defn wrap-handle-websocket-requests
  [handler websocket]
  (fn [{:keys [request-method uri] :as req}]
    (if (not= uri "/ws")
      (handler req)
      (case request-method
        :get ((:ajax-get-or-ws-handshake-fn websocket) req)
        :post ((:ajax-post-fn websocket) req)))))

(defrecord WebsocketHandlerComponent [websocket handler cancel-fn]
  component/Lifecycle
  (start [this]
    (if cancel-fn
      this
      (let [{:keys [send-fn ch-recv]} websocket
            handle-fn (handler {:send-fn send-fn})
            cancel-fn (asyncu/loop-ch handle-fn ch-recv)]
        (assoc this :cancel-fn cancel-fn))))
  (stop [this]
    (cancel-fn)
    (dissoc this :cancel-fn)))

(defn websocket-handler-component
  [handler]
  (map->WebsocketHandlerComponent {:handler handler}))
