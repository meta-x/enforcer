(ns mx.enforcer.middleware
  (:require [mx.enforcer.core :refer [enforce-map get-errors]]
            [cheshire.core :refer [generate-string]]))

(defn- default-error-handler
  [errors]
  {:status 400 :body (generate-string errors)}) ; return a json string in the form [error-obj]

(defn- new-request
  "Updates the request object with the enforced parameter values."
  [request old-params new-params]
  (->>
    new-params
    (merge old-params)
    (assoc request :params)))

(defn wrap-enforcer
  "Middleware that applies the coercion and validation rules to the request
  handler's arguments."
  ([handler query-routes]
    (wrap-enforcer handler query-routes default-error-handler))
  ([handler query-routes error-handler]
    (fn [request]
      ; unwrap the objects and apply the enforcing
      (let [[route-handler route-params] (query-routes request)
            request-params (:params request)
            old-params (merge request-params route-params)
            new-params (enforce-map route-handler old-params)]
        ; if there was any error during enforcing, execute error handler
        ; otherwise continue ring middleware processing
        (if-let [errors (get-errors new-params)]
          (error-handler errors)
          (handler (new-request request old-params new-params)))))))
