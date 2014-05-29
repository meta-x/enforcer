(ns mx.enforcer.middleware
  (:require [mx.enforcer.core :refer [enforce]])
  )

; IDEA: what if paths just called this library?
; pro: more efficient for people who want to use paths+enforcer
; con: less efficient for people who DON'T want to use paths+enforcer

(defn- get-errors [result]
  (->>
    result
    (filter :error)
    (map :error)
    (seq)))

(defn- default-error-handler [errors]
  {
    :status 400
    :body "TODO: create json array from errors (jsonify errors)" ; TODO: tbi
  })

(defn- new-request [request old-params new-params]
  (->>
    (merge old-params new-params)
    (assoc request)))

(defn wrap-enforcer
  [handler route-dispatcher error-handler]
  (fn [request]
    (let [fn-var (route-dispatcher request)
          old-params (:params request)
          new-params (enforce fn-var old-params)]
      (if-let [errors (get-errors new-params)]
        (error-handler errors)
        (handler (new-request request old-params new-params))))))
