(ns werewolfz.utils.logging
  #+clj (:require [clojure.tools.logging :as log]))

#+cljs (enable-console-print!)

(def trace println)
(def debug println)
(def info println)
(def warn println)
(def error println)
