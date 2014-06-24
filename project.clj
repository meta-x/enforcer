(defproject enforcer "0.1.0-beta2"
  :description "A library for coercion (or more correctly, type-casting) and validation of functions and their arguments."
  :url "https://github.com/meta-x/enforcer"
  :license {
    :name "The MIT License"
    :url "http://opensource.org/licenses/MIT"
  }
  :dependencies [
    [org.clojure/clojure "1.6.0"]
    [cheshire "5.3.1"]
  ]
  :deploy-repositories [
    ["clojars" {:sign-releases false}]
  ]
)
