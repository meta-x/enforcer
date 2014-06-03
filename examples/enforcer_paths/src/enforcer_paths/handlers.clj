(ns enforcer-paths.handlers
  (:require [ring.util.response :refer [response]]
            [enforcer-paths.enforcement :refer [custom-validate-fail custom-coerce-fail custom-enforcer custom-coercer custom-validator custom-enforcer-wildcards]])
  )

;;; HANDLERS

(defn index []
  (response "hey"))

(defn ^{:enforcer-ns 'enforcer-paths.enforcement :validate-fail custom-validate-fail :coerce-fail custom-coerce-fail} handler-with-args
  [^{:enforce custom-enforcer} p1
   ^{:coerce custom-coercer :coerce-fail custom-coercion-error-handler :validate custom-validator :validate-fail custom-validation-error-handler} p2
   ^{:coerce custom-coercer :validate custom-validator} p3]
  (println "--- handler-with-args")
  (println p1 (type p1))
  (println p2 (type p2))
  (println p3 (type p3))
  (response (str p1 "\n" p2 "\n" p3)))

(defn handler-with-no-args []
  (println "--- handler-with-no-args")
  (response "no args"))

(defn ^{:enforcer-ns 'enforcer-paths.enforcement} handler-wildcard [
    ^{:enforce custom-enforcer-wildcards} wildcards
  ]
  (println "--- handler-wildcard")
  (response wildcards))
