(ns mx.enforcer.core
  )



;;; "COERCION"

; NB: coercion in computer science means the implicit process of converting a
; value of one type to another type
; the term is not totally wrong for how it is used in this library, since that's
; what we're trying to achieve, to have the library automatically convert the
; value between types, even though it is done in an user-defined function

; this process is applied ot a single value at a time

(defn- coerce [coerce-fn param arg on-coerce-fail]
  "Applies the `coerce-fn` with the `arg` as argument. If `coerce-fn` throws
  an exception, `on-coerce-fail` will be called and supplied with the available
  information at the time."
  (try
    (coerce-fn arg)
    (catch Exception e
      (on-coerce-fail e param arg))))



;;; VALIDATION

(defn- validate [validate-fn param arg on-validate-fail]
  (try
    (validate-fn arg)
    (catch Exception e)
      (on-validate-fail e param arg)))



;;; ENFORCER

(defn- default-on-coerce-fail [exception param arg]
  {
    :error
    {
      :msg (str "Failed to cast " param " with value " arg ". Exception: " exception)
      :param param
      :arg arg
      :exception exception
    }
  })
(defn- default-on-validate-fail [exception param arg]
  {
    :error
    {
      :msg (str "Failed to validate " param " with value " arg ". Exception: " exception)
      :param param
      :arg arg
      :exception exception
    }
  })

(defn- default-enforcer [arg _] ; default validate/coerce function (identity)
  arg)

(defn run [fnvar args]
  ; obtain the general metadata from the function
  ; `validate`: general validation function (for more complex validations)
  ; `on-validate-fail`: general validation error handling function
  ; `on-coerce-fail`: general coercion error handling function
  (let [metadata (meta fnvar)
        params (first (:arglists metadata))
        fn-validate (get metadata :validate default-enforcer)
        fn-validate-fail (get metadata :validate-fail default-on-validate-fail)
        fn-coerce-fail (get metadata :coerce-fail default-on-coerce-fail)]
    ; apply enforcer rules: TODO: this isn't really functional :\
    (map (fn [param arg]
      ; argument-specific functions (attached to fnvar's arglists)
      ; `enforce`: coercion+validation function
      ; `coerce`: coercion function
      ; `coerce-fail`: coercion error handling function
      ; `validate`: validation function
      ; `validate-fail`: validation error handling function
      (let [param-meta (meta param)
            enforce-fn (:enforce param-meta)
            coerce-fn (get param-meta :coerce default-enforcer)
            coerce-fail (get param-meta :coerce-fail fn-coerce-fail)
            validate-fn (get param-meta :validate fn-validate)
            validate-fail (get param-meta :validate-fail fn-validate-fail)]
        ; TODO:
        ; apply the operations in the correct way
        ;
        ; if `enforce`exists, apply enforce
        ; else
        ;   if coerce exists, apply coerce
        ;   if validate exists, apply validate
        ;
        ; if coerce-fail exists, use coerce-fail
        ; else if on-coerce-fail exists, use on-coerce-fail
        ; else, use default-on-coerce-fail
        ;
        ; if validate-fail exists, use validate-fail
        ; else if on-validate-fail exists, use on-validate-fail
        ; else, use default-on-validate-fail
        ;
        ; what to return?
        ; - coerced values/nil (?)
        ; - the enforced/coerced+validated value
        (if (not (nil? enforce))
          (enforce-fn param arg coerce-fail validate-fail)
          (->
            (coerce coerce-fn param arg coerce-fail)
            (validate validate-fn param arg validate-fail)))
        )
    ) params args)))



(defn enforce [fnvar args]
  ; coerce and validate the arguments against the function's definition
  (run fnvar args))

(defn enforce-all [fnvars largs]
  ; coerce and validate the list of arguments against the list of function definitions
  (map enforce fnvar largs)
  )