(ns mx.enforcer.core
  (:require [mx.enforcer.coercion :refer [coerce default-on-coerce-fail]]
            [mx.enforcer.validation :refer [validate default-on-validate-fail]]))

;;; ENFORCER

(defn- default-enforcer
  [_ arg] ; default validate/coerce function (identity)
  arg)

(defn- get-param-meta
  "Helper that extracts the metadata from function parameters."
  [param-meta ns key default]
  (or
    (some->>
      (get param-meta key)
      (ns-resolve ns))
    default))

(defn- coerce-validate
  "Helper that applies coercion and validation or error handlers in case any of
  the functions fails."
  [coerce-fn validate-fn param arg on-coerce-fail on-validate-fail]
  (try
    (let [coerced-val (coerce-fn param arg)]
      (try
        (validate-fn param coerced-val)
        (catch Exception e
          (on-validate-fail e param arg))))
    (catch Exception e
      (on-coerce-fail e param arg))))

(defn run
  [fnvar args]
  ; obtain the general metadata from the function
  (let [metadata (meta fnvar)
        params (first (:arglists metadata))
        fn-validate-fail (get metadata :validate-fail default-on-validate-fail) ; `on-validate-fail`: general validation error handling function
        fn-coerce-fail (get metadata :coerce-fail default-on-coerce-fail) ; `on-coerce-fail`: general coercion error handling function
        ns (:enforcer-ns metadata)]
    ; apply enforcer rules: TODO: is this "functional"? doesn't feel like it :\
    (->>
      params
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
            (if-not (nil? enforce-fn)
              (enforce-fn param arg)
              (coerce-validate coerce-fn validate-fn param arg coerce-fail validate-fail)
            ))))
      (zipmap (map keyword params))))) ; {:a 1 :b 2 ... :key value}

(defn get-errors
  [result]
  "From an `enforce` result, retrieve a sequence of errors or nil if there are
  none. Use case is in (if-let [errors (get-errors (enforce fnvar args))] ...)"
  (->>
    (map #(if-let [error (:error %2)] error nil) (keys result) (vals result))
    (filter (comp not nil?))
    (seq))) ; nil or ({error obj})

(defn enforce
  "Coerce and validate the arguments against the function's definition"
  [fnvar args]
  (run fnvar args))

(defn enforce-all
  "Coerce and validate the list of arguments against the list of function definitions"
  [fnvars largs]
  (map enforce fnvars largs))
