(ns mx.enforcer.core
  (:require [mx.enforcer.coercion :refer [coerce default-on-coerce-fail]]
            [mx.enforcer.validation :refer [validate default-on-validate-fail]]))

;;; ENFORCER

(defn- default-enforcer
  "Default validate/coerce function (identity)"
  [_ arg]
  arg)

(defn- get-enf-fn
  "Helper that returns the enforcing function (coercion, validation or enforcment)
  for the given parameter or, if not found, the default."
  [param-meta ns key default]
  (or
    ; try to get the function and resolve it
    (some->>
      (get param-meta key)
      (ns-resolve ns))
    ; or return the default value
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
  "Takes a function var and a map of parameter names and values and applies the
  enforcement rules specified for the function's parameters."
  [fnvar args]
  ; obtain the general metadata from the function
  (let [metadata (meta fnvar)
        params (first (:arglists metadata))
        ns (:enforcer-ns metadata)]
    ; returns a (mixed together) sequence of
    ; - the enforced/coerced+validated value
    ; - error maps from the error-handlers
    (->>
      params
      (map
        (fn [param]
          (let [arg (get args (keyword param))
                param-meta (meta param)
                enforce-fn (get-enf-fn param-meta ns :enforce nil)] ; coercion+validation function
            (if-not (nil? enforce-fn)
              (enforce-fn param arg)
              ; fn-validate/coerce-fail could be done at the beginning of this
              ; function, but why do that, if they might not be needed? rather
              ; do it later, even if it might be repeated
              (let [fn-validate-fail (get metadata :validate-fail default-on-validate-fail) ; general validation error handling function
                    fn-coerce-fail (get metadata :coerce-fail default-on-coerce-fail) ; general coercion error handling function
                    coerce-fn (get-enf-fn param-meta ns :coerce default-enforcer) ; coercion function
                    coerce-fail (get-enf-fn param-meta ns :coerce-fail fn-coerce-fail) ; coercion error handling function
                    validate-fn (get-enf-fn param-meta ns :validate default-enforcer) ; validation function
                    validate-fail (get-enf-fn param-meta ns :validate-fail fn-validate-fail)] ; validation error handling function
                (coerce-validate coerce-fn validate-fn param arg coerce-fail validate-fail))))))
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
