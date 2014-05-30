(ns enforcer-paths.core
  (:require [mx.paths.core :refer [router]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :refer [response]]
            [mx.enforcer.middleware :refer [wrap-enforcer]]
    )
  )

;;; enforcer

(defn custom-validate-fail [exception param arg]
  (println "custom-validation-fail")
  {:error {}})

(defn custom-coerce-fail [exception param arg]
  (println "custom-coerce-fail")
  {:error {}})


(defn custom-enforcer [arg]
  (println "custom-enforcer")
  arg)


(defn custom-coercer [arg]
  (println "custom-coercer")
  arg)

(defn custom-coercion-error-handler [exception param arg]
  (println "custom-coercion-error-handler")
  {:error {}})

(defn custom-validator [arg]
  (println "custom-validator")
  arg)

(defn custom-validation-error-handler [exception param arg]
  (println "custom-validation-error-handler")
  {:error {}})



;;; handlers

(defn index []
  (response "hey"))

(defn ^{:validate-fail custom-validate-fail :coerce-fail custom-coerce-fail} handler-with-args
  [^{:enforce custom-enforcer} p1
   ^{:coerce custom-coercer :validate custom-validator} p2]
  (println "--- handler-with-args")
  (println p1)
  (println p2)
  (response (str p1 "\n" p2)))

(defn handler-with-no-args []
  (println "--- handler-with-no-args")
  (response "no args"))

(defn handler-wildcard [
    ^{:enforce custom-enforcer} wildcards
  ]
  (println "--- handler-wildcard")
  (response wildcards))

;;; paths

(def routes [
  "/" {:get #'index}
  "/with/args" {:get #'handler-with-args}
  "/with/no/args" {:get #'handler-with-no-args}
  "/this/accepts/:wildcards/yeah" {:get #'handler-wildcard}
  ])
(def route-dispatcher (create-tree routes))



(def app
  (->
    (router routes) ; TODO: pass route-tree here
    (wrap-enforcer route-dispatcher)
    (wrap-keyword-params)
    (wrap-params)
    ))
