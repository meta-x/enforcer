(ns mx.enforcer.middleware
  )

; description
; integrated into paths
; retrieve the handler-var from paths' routes tree
; (let [fnvar (get-handler (:uri request))]
;    ...)

; alter the request :params map with the coerced values before calling the handler/next middleware

; IDEA: what if paths just called this library?
; pro: more efficient for people who want to use paths+enforcer
; con: less efficient for people who DON'T want to use paths+enforcer









;;; MIDDLEWARE

(defn wrap-enforcer
  [handler]
  (fn [request]

    (handler request)
    ))
