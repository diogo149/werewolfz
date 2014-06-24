(ns werewolfz.timer
  (:require [com.stuartsierra.component :as component]
            [werewolfz.utils.timer :as timeru]))

(defrecord TimerComponent []
  component/Lifecycle
  (start [this]
    this)
  (stop [this]
    (timeru/cancel-intervals)
    (timeru/cancel-timeouts)))

(defn timer-component
  []
  (TimerComponent.))
