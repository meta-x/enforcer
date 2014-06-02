(ns enforcer-paths.core
  (:require [mx.paths.core :refer [create-routes-tree bind-query-routes router-with-tree]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :refer [response]]
            [cheshire.core :refer [generate-string]]
            [mx.enforcer.core :refer [enforce]]
            [mx.enforcer.middleware :refer [wrap-enforcer]]
    ))

;;; enforcer

(defn custom-validate-fail [exception param arg]
  (println "custom-validation-fail")
  {:error {
    :msg (str "1. " param " is not valid.")
    :param param
    :arg arg
    :exception (str exception)
  }})

(defn custom-coerce-fail [exception param arg]
  (println "custom-coerce-fail")
  {:error {
    :msg (str "1. " param " could not be cast.")
    :param param
    :arg arg
    :exception (str exception)
  }})



(defn custom-enforcer [param arg coerce-fail validate-fail]
  (println "custom-enforcer")
  (println param "\t" arg)
  (println arg)
  (println coerce-fail)
  (println validate-fail)
  (if (nil? arg)
    {:error {
      :msg (str param " missing.")
      :param param
      :arg arg
      ;:exception
    }}
    arg))

(defn custom-coercer [param arg]
  (println "custom-coercer")
  (println arg)
  (Integer/parseInt arg))

(defn custom-validator [param arg]
  (println "custom-validator")
  (println arg)
  (if (> arg 5)
    (throw (Exception. (str param " can't be > than 5.")))
    arg))

(defn custom-coercion-error-handler [exception param arg]
  (println "custom-coercion-error-handler")
  {:error {
    :msg (str "2. " param " is not valid.")
    :param param
    :arg arg
    :exception (str exception)
  }})

(defn custom-validation-error-handler [exception param arg]
  (println "custom-validation-error-handler")
  {:error {
    :msg (str "2. " param " could not be cast.")
    :param param
    :arg arg
    :exception (str exception)
  }})

(defn custom-enforcer-wildcards [param arg coerce-fail validate-fail]
  (println "custom-enforcer")
  (println param "\t" arg)
  (println arg)
  (if (nil? arg)
    {:error {
      :msg (str param " missing.")
      :param param
      :arg arg
      ;:exception
    }}
    arg))



;;; handlers

(defn index []
  (response "hey"))

(defn ^{:enforcer-ns 'enforcer-paths.core :validate-fail custom-validate-fail :coerce-fail custom-coerce-fail} handler-with-args
  [^{:enforce custom-enforcer} p1
   ^{:coerce custom-coercer :validate custom-validator} p2]
  (println "--- handler-with-args")
  (println p1)
  (println p2)
  (response (str p1 "\n" p2)))

(defn handler-with-no-args []
  (println "--- handler-with-no-args")
  (response "no args"))

(defn ^{:enforcer-ns 'enforcer-paths.core} handler-wildcard [
    ^{:enforce custom-enforcer-wildcards} wildcards
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
(def routes-tree (create-routes-tree routes))

(def app
  (->
    (router-with-tree routes-tree)
    (wrap-enforcer (bind-query-routes routes-tree))
    (wrap-keyword-params)
    (wrap-params)
    ))
