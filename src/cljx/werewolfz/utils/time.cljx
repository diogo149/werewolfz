(ns werewolfz.utils.time)

(defn date->hh:MM:ss
  [date]
  #+cljs
  (.. date toTimeString (replace #".*(\d{2}:\d{2}:\d{2}).*" "$1"))
  #+clj
  (throw (UnsupportedOperationException. "TODO implement")))
