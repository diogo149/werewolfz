(defproject werewolfz "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :jvm-opts ["-Xmx512m" "-Xms512m"]
  :min-lein-version "2.0.0"
  :profiles
  {:dev [:clj {:dependencies [[org.clojure/tools.namespace "0.2.4"]]
               :plugins [[lein-shell "0.4.0"]]
               :source-paths ["src/dev"]
               :repl-options {:init-ns user}
               :aliases
               { ;; cleaning
                "clean-cljs" ["shell" "rm" "-rf" "resources/public/cljs" ","]
                "clean-cljx" ["shell" "rm" "-rf" "cljx-target/" ","]
                "clean-all" ["do" "clean," "clean-cljs," "clean-cljx,"]
                ;; clojurescript
                "cljs1" ["do" "clean-cljs,"
                         "with-profile" "cljs" "cljsbuild" "once" "prod,"]
                "cljs" ["do" "clean-cljs,"
                        "with-profile" "cljs" "cljsbuild" "auto" "dev"]
                ;; cljx
                "cljx1" ["do" "clean-cljx,"
                         "with-profile" "cljx" "cljx" "once,"]
                "cljx" ["do" "clean-cljx,"
                        "with-profile" "cljx" "cljx" "auto,"]
                ;; clj
                "clj" ["do" "clean,"
                       "compile,"
                       "repl,"]
                "web" ["do" "clean-all,"
                       "cljx1",
                       "cljs1",
                       "with-profile" "clj" "run",]}}]
   :cljx {:plugins [[com.keminglabs/cljx "0.4.0"]]
          :cljx {:builds [{:source-paths ["src/cljx"]
                           :output-path "cljx-target/cljs"
                           :rules :cljs}
                          {:source-paths ["src/cljx"]
                           :output-path "cljx-target/clj"
                           :rules :clj}]}}
   :shared {:dependencies [[org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                           [com.taoensso/sente "0.14.1"]
                           #_[org.clojure/core.match "0.2.1"]]}
   :clj [:shared
         {:main werewolfz.main
          :dependencies [[org.clojure/tools.logging "0.3.0"]
                         [org.clojure/data.json "0.2.4"]
                         [compojure "1.1.8"]
                         [ring "1.2.1"]
                         [ring/ring-json "0.3.1"]
                         [ring/ring-anti-forgery "1.0.0"]
                         [com.stuartsierra/component "0.2.1"]
                         [hiccup "1.0.5"]
                         ;; [korma "0.3.2"]
                         ;; http://mvnrepository.com/artifact/org.postgresql/postgresql
                         ;; [org.postgresql/postgresql "9.3-1101-jdbc41"]
                         [http-kit "2.1.18"]]
          :source-paths ["src/clj"
                         "cljx-target/clj"
                         "src/macros"]}]
   :cljs [:shared
          {:dependencies [[org.clojure/tools.reader "0.8.3"] ;; 0.8.4 doesn't work
                          [org.clojure/clojurescript "0.0-2227"]
                          ;; [secretary "1.1.1"]
                          [reagent "0.4.2"]
                          [cljs-http "0.1.11"]]
           :plugins [[lein-cljsbuild "1.0.3"]]
           :cljsbuild
           {:builds [{:id "dev"
                      :source-paths ["src/cljs"
                                     "src/macros"
                                     "cljx-target/cljs"]
                      :compiler
                      {:output-to "resources/public/cljs/all.js"
                       :output-dir "resources/public/cljs/dev"
                       :optimizations :none
                       :source-map "resources/public/cljs/all.js.map"
                       :externs ["react/externs/react.js"]}}
                     {:id "prod"
                      :source-paths ["src/cljs"
                                     "src/macros"
                                     "cljx-target/cljs"]
                      :compiler
                      {:output-to "resources/public/cljs/all.js"
                       :output-dir "resources/public/cljs/prod"
                       :optimizations :advanced
                       :pretty-print false
                       :output-wrapper false
                       :preamble ["reagent/react.min.js"]
                       :externs [ ;; "jquery/externs/jquery.js"
                                 "src/js/extern.js"]
                       :closure-warnings
                       {:non-standard-jsdoc :off}}}]}}]
   :uberjar [:shared
             :clj
             {:aot :all
              :main werewolfz.main
              :source-paths ["src/clj"
                         "cljx-target/clj"
                         "src/macros"]
              :uberjar-name "werewolfz-standalone.jar"}]})
