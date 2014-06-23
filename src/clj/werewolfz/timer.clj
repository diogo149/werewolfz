(ns werewolfz.timer
  (:require [com.stuartsierra.component :as component]
            [werewolfz.utils.timer :as timer]))

(defrecord TimerComponent []
  component/Lifecycle
  (start [this]
    this)
  (stop [this]
    (timer/cancel-intervals)
    (timer/cancel-timeouts)))

(defn timer-component
  []
  (TimerComponent.))
