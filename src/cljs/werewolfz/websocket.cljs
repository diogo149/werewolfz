(ns werewolfz.websocket
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [taoensso.sente :as sente]
            [werewolfz.utils.logging :as log]
            werewolfz.logic.client
            [cljs.core.async :as async]))

;; -----------
;; web sockets
;; -----------

(let [socket-map (sente/make-channel-socket! "/ws" {:type :auto})
      {:keys [ch-recv ; ChannelSocket's receive channel
              send-fn ; ChannelSocket's send API fn
              state ; Watchable, read-only atom
              chsk]} socket-map
      handler (werewolfz.logic.client/handler {:send-fn send-fn})]

  (def send!
    "Function to send a message over a websocket connection. Takes in either
     a single data structure to send of the form [:namespaced/keyword data]
     or 3 arguments with the second and third being a timeout in msec and a
     callback function, respectively."
    send-fn)

  (go
    (loop []
      (let [msg (<! ch-recv)]
        (handler msg)
        (recur)))))
