(require '[figwheel-sidecar.repl :as r]
         '[figwheel-sidecar.repl-api :as ra])

(ra/start-figwheel!
  {:figwheel-options {:css-dirs ["resources/public/css"]}
   :build-ids        ["dev" "devcards"]
   :all-builds
                     [{:id           "dev"
                       :figwheel     true
                       :source-paths ["src"]
                       :compiler     {:main                 'bowling-kata.boot
                                      :asset-path           "js"
                                      :output-to            "resources/public/js/main.js"
                                      :output-dir           "resources/public/js"
                                      :verbose              true
                                      :source-map-timestamp true}}
                      {:id           "devcards"
                       :source-paths ["src"]
                       :figwheel     {:devcards true}       ;; <- note this
                       :compiler     {:main                 'bowling-kata.cards
                                      :recompile-dependents true
                                      :asset-path           "js"
                                      :output-to            "resources/public/js/cards.js"
                                      :output-dir           "resources/public/js"
                                      :source-map-timestamp true}}]})

(ra/cljs-repl)
