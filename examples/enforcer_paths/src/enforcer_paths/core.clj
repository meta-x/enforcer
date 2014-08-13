(ns enforcer-paths.core
  (:require [mx.paths.core :refer [route query]]
            [mx.paths.tree :refer [create-routes-tree]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [mx.enforcer.middleware :refer [wrap-enforcer]]
            [enforcer-paths.handlers :refer [index handler-with-args handler-with-no-args handler-wildcard]]
    ))

;;; PATHS

(def routes [
  "/" {:get #'index}
  "/with/args" {:get #'handler-with-args}
  "/with/no/args" {:get #'handler-with-no-args}
  "/this/accepts/:wildcards/yeah" {:get #'handler-wildcard}
  ])
(def routes-tree (create-routes-tree routes))

(def app
  (->
    routes-tree
    (route)
    (wrap-enforcer (query routes-tree))
    (wrap-keyword-params)
    (wrap-params)))
