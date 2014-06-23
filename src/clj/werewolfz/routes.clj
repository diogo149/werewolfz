(ns werewolfz.routes
  (:require [compojure.core :as compojure]
            [compojure.route :as route]
            [werewolfz.utils.server :as serveru]
            [werewolfz.utils.random :as randomu]
            werewolfz.pages.home))

(compojure/defroutes app-routes
  (compojure/GET "/" req (-> req
                             werewolfz.pages.home/home-page
                             serveru/html-response
                             (assoc-in [:session :uid] (randomu/uuid))))
  (route/resources "/")
  (route/not-found "Not Found"))
