(defproject enforcer_paths "0.2"
  :description "An example of enforcer with paths."
  :license {
    :name "The MIT License"
    :url "http://opensource.org/licenses/MIT"
  }
  :dependencies [
    [org.clojure/clojure "1.6.0"]
    [paths "0.1.0"]
    [enforcer "0.1.0"]
    [ring/ring-core "1.3.0"]
    [ring/ring-jetty-adapter "1.3.0"]
    [cheshire "5.3.1"]
  ]
  :profiles {
    :dev {
      :plugins [
        [lein-ring "0.8.10"]
        [ring/ring-devel "1.2.2"]
      ]
    }
  }
  :ring {:handler enforcer-paths.core/app}
)
