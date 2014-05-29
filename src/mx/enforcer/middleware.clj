(ns mx.enforcer.middleware
  (:require [mx.enforcer.core :refer [enforce]]
            [cheshire.core :refer [generate-string]])
  )

(defn- get-errors [result]
  (->>
    result
    (filter :error)
    (map :error) ; remove the top-level :error key
    (seq))) ; nil or ({error obj})

(defn- default-error-handler [errors]
  {
    :status 400
    :body (generate-string errors) ; return a json string in the form [{error obj}]
  })

(defn- new-request [request old-params new-params]
  "Updates the request object with the enforced parameter values."
  (->>
    (merge old-params new-params)
    (assoc request)))

(defn wrap-enforcer
  ([handler route-dispatcher]
    (wrap-enforcer handler route-dispatcher default-error-handler))
  ([handler route-dispatcher error-handler]
    (fn [request]
      ; unwrap the objects and apply the enforcing
      (let [fn-var (route-dispatcher request)
            old-params (:params request)
            new-params (enforce fn-var old-params)]
        ; if there was any error during enforcing, execute error handler
        ; otherwise continue ring middleware processing
        (if-let [errors (get-errors new-params)]
          (error-handler errors)
          (handler (new-request request old-params new-params)))))))
