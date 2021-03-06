(ns user
  "Tools for interactive development with the REPL. This file should
not be included in a production build of the application."
  (:require
   [clojure.java.io :as io]
   [clojure.java.javadoc :refer (javadoc)]
   [clojure.pprint :refer (pprint)]
   [clojure.reflect :refer (reflect)]
   [clojure.repl :refer (apropos dir doc find-doc pst source)]
   [clojure.set :as set]
   [clojure.string :as str]
   [clojure.test :as test]
   [clojure.tools.namespace.repl :as ctnr]
   [clojure.core.async :as async]
   [com.stuartsierra.component :as component]
   [werewolfz.app :as app]
   [werewolfz.utils.timer :as timeru]
   [werewolfz.utils.random :as randomu]
   [werewolfz.logic.state :as state]
   [werewolfz.logic.game :as game]))

(def system
    "A Var containing an object representing the application under development."
    nil)

(defn init
  "Creates and initializes the system under development in the Var #'system."
  []
  (alter-var-root #'system
    (constantly (app/main-system {}))))

(defn start
  "Starts the system running, updates the Var #'system."
  []
  (alter-var-root #'system component/start))

(defn stop
  "Stops the system if it is currently running, updates the Var #'system."
  []
  (alter-var-root #'system
    (fn [s] (when s (component/stop s)))))

(defn go
  "Initializes and starts the system running."
  []
  (init)
  (start))

(defn reset
  "Stops the system, reloads modified source files, and restarts it."
  []
  (stop)
  (ctnr/refresh :after 'user/go))
