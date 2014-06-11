(ns werewolfz.pages.home
  (:require [hiccup.core :as hiccup]))

(defn home-page
  [{:keys [production?] :as req}]
  (hiccup/html
   (list
    "<!DOCTYPE html>"
    [:html
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:http-equiv "X-UA-Compatible"
              :content "IE=edge,chrome=1"}]
      [:meta
       {:name "viewport"
        :content "width=device-width, initial-scale=1.0, maximum-scale=2.0"}]
      ;; [:link {:rel "shortcut icon", :href "/icon/favicon.ico"}]
      [:title "One Night..."]]
     [:body
      [:div {:id "content"} "Loading..."]
      (when-not production?
        (list [:script {:type "text/javascript"
                        :src "/bower_components/react/react.js"}]
              [:script {:type "text/javascript"
                        :src "/cljs/dev/goog/base.js"}]))
      [:script {:type "text/javascript"
                :src "/cljs/all.js"}]
      (when-not production?
        [:script {:type "text/javascript"}
         "goog.require(\"werewolfz.core\");"])]])))
