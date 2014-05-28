(ns mx.enforcer.core
  )




;;; COERCION

; TODO: tbi
; coerce-fn is the function that applies the coercion
; should follow some convention that returns a specific error
; so that the root caller knows what's wrong
; i.e. imagine this being called N levels deep e.g. in the context of a web-api
; the caller should know where the error happened exactly so that it returns a correct return value
; 400 bad request, etc

; idea is:
; have the "type" definition somewhere in a "schema"
; type definition should be mapped to some coercion function
; coercion functions are either default supplied by library or user defined
; grab that schema and apply coercion function

; define coercion
; that's gonna be harder
; a simple library where a map is defined
; :param-name :param-type :on-fail-fn :coerce-fn
; and the library takes care of trying to coerce the param to the correct type
; or calls the user-defined :coerce-fn to do the work


(defn- coerce [value coerce-fn]
  (coerce-fn value)
  )



(defn to-str [v]
  (str v))

(defn to-int [v]
  (Integer/parseInt v))

(defn to-double [v]
  (Double/parseDouble v))

(defn to-long [v]
  (Long/parseLong v))

; ; coercion definition (what and what to coerce)
; {
;   :param1-name to-str
;   :param2-name to-int
;   :param3-name to-double
;   ...
; }


; ; create a function that
; ; accesses the arguments of a function
; ; makes the coercion based on the coercion definition
; ; replaces the arguments with the new values
; ; basically I want to do an interceptor that changes the values of a function
; ; and use it as if it were an annotation

; @coercer
; (defn x ^{:coercer-policy } [a b c]
;   ...)

; coercer will
; intercept the call to x
; coerce a b c to d e f
; call (x d e f)



; ------------------------------------------------

; v1
; define coercions definition
; take definition and argument list as params
; return coerced argument list


;;; VALIDATION

; TODO: tbi
; define validation info somewhere in a "schema" definition
; validation info is a user-defined function
; should follow some convention that returns a specific error
; so that the root caller knows what's wrong

; several levels of validation
; first level validation: existence (nil/non-nil)
; second level validation: type validation (String, Double)
; third level validation: argument specific (length, enum, etc)


; define validation (using validateur)
; ideally in the korma models
; or at least in the same module

; implement a middleware function
; wrap-something

(defn validate-str [s]
  (println "do some string validation here")
  true)

(defn validate-int [s]
  (println "do some int validation here")
  true)

(defn my-fn [^{:vld-fn validate-str} arg1 ^{:vld-fn validate-int} arg2]
  (println "my-fn")
  (println arg1)
  (println arg2)
)

(defn- get-arglist [handler]
  "Helper for retrieving the argument list from a function (var)."
  (->
    (meta handler)
    (:arglists)
    (first)))

(defn get-vld-fns [fn-params]
  (->>
    fn-params
    (map meta)
    (map :vld-fn)
  ))

(defn exec-vld-fn [vld-fn arg]
  ; TODO: catch exceptions
  (if (not (nil? vld-fn))
    (apply vld-fn arg)))

(defn validate-args [fn-params fn-args]
  (let [vld-fns (get-vld-fns fn-params)]
    (map exec-vld-fn vld-fns fn-args)
    ))



; (validate-args args)


;;; MIDDLEWARE

(defn wrap-enforcer
  [handler]
  (fn [request]




    (handler request)
    ))



