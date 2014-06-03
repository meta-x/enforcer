(ns mx.enforcer.core
  )

; TODO:
; auto-infer the namespace by using the :ns meta
; enforce's return value (or another function) should not be a map, but a vector in the same order as the arguments?

;;; "COERCION"

; NB: coercion in computer science means the implicit process of converting a
; value of one type to another type
; the term is not totally wrong for how it is used in this library, since that's
; what we're trying to achieve, to have the library automatically convert the
; value between types, even though it is done in an user-defined function

; this process is applied on a single value at a time

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
    (catch Exception e
      (on-validate-fail e param arg))))



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

(defn- get-param-meta [param-meta ns key default]
  "Helper that extracts the metadata from function parameters."
  (or
    (some->>
      (get param-meta key)
      (ns-resolve ns))
    default
    ))

(defn- coerce-validate [coerce-fn validate-fn param arg on-coerce-fail on-validate-fail]
  "Helper that applies coercion and validation or error handlers in case any of
  the functions fails."
  (try
    (let [coerced-val (coerce-fn param arg)]
      (try
        (validate-fn param coerced-val)
        (catch Exception e
          (on-validate-fail e param arg))))
    (catch Exception e
      (on-coerce-fail e param arg))))

(defn run [fnvar args]
  ; obtain the general metadata from the function
  ; `on-validate-fail`: general validation error handling function
  ; `on-coerce-fail`: general coercion error handling function
  (let [metadata (meta fnvar)
        params (first (:arglists metadata))
        fn-validate-fail (get metadata :validate-fail default-on-validate-fail)
        fn-coerce-fail (get metadata :coerce-fail default-on-coerce-fail)
        ns (:enforcer-ns metadata)]
    ; apply enforcer rules: TODO: is this "functional"? doesn't feel like it :\
    (->>
      (map
        (fn [param]
          ; argument-specific functions (attached to fnvar's arglists)
          ; `enforce`: coercion+validation function
          ; `coerce`: coercion function
          ; `coerce-fail`: coercion error handling function
          ; `validate`: validation function
          ; `validate-fail`: validation error handling function
          (let [arg (get args (keyword param))
                param-meta (meta param)
                enforce-fn (get-param-meta param-meta ns :enforce nil)
                coerce-fn (get-param-meta param-meta ns :coerce default-enforcer)
                coerce-fail (get-param-meta param-meta ns :coerce-fail fn-coerce-fail)
                validate-fn (get-param-meta param-meta ns :validate default-enforcer)
                validate-fail (get-param-meta param-meta ns :validate-fail fn-validate-fail)]
            ; apply the operations in the correct way, general workflow is the following
            ;
            ; if `enforce` exists, apply `enforce`
            ; else
            ;   if `coerce` exists, apply `coerce`
            ;   if `validate` exists, apply `validate`
            ;
            ; if `coerce-fail` exists, use `coerce-fail`
            ; else if `on-coerce-fail` exists, use `on-coerce-fail`
            ; else, use `default-on-coerce-fail`
            ;
            ; if `validate-fail` exists, use `validate-fail`
            ; else if `on-validate-fail` exists, use `on-validate-fail`
            ; else, use `default-on-validate-fail`
            ;
            ; returns a sequence of
            ; - the enforced/coerced+validated value
            ; - error maps from the error-handlers
            ; mixed together
            (if (not (nil? enforce-fn))
              (enforce-fn param arg)
              (coerce-validate coerce-fn validate-fn param arg coerce-fail validate-fail)
            )))
        params)
      (zipmap (map keyword params)) ; {:a 1 :b 2 ... :key value}
    )
  ))



(defn enforce [fnvar args]
  ; coerce and validate the arguments against the function's definition
  (run fnvar args))

(defn enforce-all [fnvars largs]
  ; coerce and validate the list of arguments against the list of function definitions
  (map enforce fnvars largs))
