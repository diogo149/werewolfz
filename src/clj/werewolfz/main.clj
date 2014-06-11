(ns werewolfz.main
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [werewolfz.app :as app]))

(defn -main [& args]
  (component/start
   (app/main-system {:production? true})))
