(ns mx.enforcer.core
  (:require [slingshot.slingshot :refer [try+ throw+]]
            [mx.enforcer.coercion :refer [coerce default-on-coerce-fail]]
            [mx.enforcer.validation :refer [validate default-on-validate-fail]]))

;;; ENFORCER

(defn- default-enforcer
  "Default validate/coerce function (identity)"
  [_ arg]
  arg)

(defn- get-enf-fn
  "Helper that returns the enforcing function (coercion, validation or enforcment)
  for the given parameter or, if not found, the default."
  [param-meta fn-ns k default]
  (or
    ; try to get the function and resolve it
    (some->>
      k
      (get param-meta)
      (ns-resolve fn-ns))
    ; or return the default value
    default))

(defn- coerce-validate
  "Helper that applies coercion and validation or error handlers in case any of
  the functions fails."
  [coerce-fn validate-fn param arg on-coerce-fail on-validate-fail]
  (try+
    (let [coerced-val (coerce-fn param arg)]
      (try+
        (validate-fn param coerced-val)
        (catch Object e
          (on-validate-fail e param arg))))
    (catch Object e
      (on-coerce-fail e param arg))))


(defn- enforce-cv
  "This helper function decodes the coercion and validation functions from the parameter
  and executes them. Uses defaults when not found. This only runs if no general param `enforcer`
  was defined."
  [fn-ns fn-meta param-meta param arg]
  (let [fn-validate-fail (get fn-meta :validate-fail default-on-validate-fail) ; general validation error handling function
        fn-coerce-fail (get fn-meta :coerce-fail default-on-coerce-fail) ; general coercion error handling function
        coerce-fn (get-enf-fn param-meta fn-ns :coerce default-enforcer) ; coercion function
        coerce-fail (get-enf-fn param-meta fn-ns :coerce-fail fn-coerce-fail) ; coercion error handling function
        validate-fn (get-enf-fn param-meta fn-ns :validate default-enforcer) ; validation function
        validate-fail (get-enf-fn param-meta fn-ns :validate-fail fn-validate-fail)] ; validation error handling function
    (coerce-validate coerce-fn validate-fn param arg coerce-fail validate-fail)))

(defn- enforce-param
  "Helper function that does the whole enforcement process on a parameter."
  [fn-ns fn-meta param arg]
  (let [param-meta (meta param)]
    (if-let [enforce-fn (get-enf-fn param-meta fn-ns :enforce nil)]
      ; use enforce (coercion+validation) function if found
      (enforce-fn param arg)
       ; otherwise default to coerce and/or validate
      (enforce-cv fn-ns fn-meta param-meta param arg))))


; internal helpers that decode the metadata from the `fnvar` and execute the enforcement

(defn- run-aux [fn-meta params parargs]
  (let [fn-ns (:enforcer-ns fn-meta)
        kparams (map keyword params)]
    ; returns a (mixed together) sequence of
    ; - the enforced/coerced+validated value
    ; - error maps from the error-handlers
    (->>
      params
      (map
        ; % = param
        ; (get parargs (keyword %)) = arg
        ; we need to keep `param` because converting it to a keyword removes the metadata
        #(enforce-param fn-ns fn-meta % (get parargs (keyword %))))
      (zipmap kparams)))) ; returns {:param arg|error}

(defn- run-list [fnvar args]
  (let [fn-meta (meta fnvar)
        params (first (:arglists fn-meta))
        kparams (map keyword params)
        parargs (zipmap kparams args)]
    (run-aux fn-meta params parargs)))

(defn- run-map [fnvar parargs]
  (let [fn-meta (meta fnvar)
        params (first (:arglists fn-meta))]
    (run-aux fn-meta params parargs)))


; public api

(defn get-errors
  [result]
  "From an `enforce` result, retrieve a sequence of errors or nil if there are
  none. Use case is in (if-let [errors (get-errors (enforce fnvar args))] ...)"
  (->>
    (map #(if-let [error (:error %2)] error nil) (keys result) (vals result))
    (filter (comp not nil?))
    (seq))) ; nil or ({error obj})

(defn enforce-map
  "Coerce and validate the arguments against the function's definition.
  `parargs` is a map in {:param arg} format."
  [fnvar parargs]
  (run-map fnvar parargs))

(defn enforce-list
  "Coerce and validate the arguments against the function's definition.
  `largs` is a list of arguments."
  [fnvar largs]
  (run-list fnvar largs))

; (defn enforce-all
;   "Coerce and validate the list of arguments against the list of function definitions"
;   [fnvars largs]
;   (map enforce fnvars largs))


(defn enforce-and-exec
  "As the name suggests, this function takes a fnvar and a list of arguments,
  does the enforcement on the arguments and, if successful, executes fnvar.
  Throws an exception if otherwise."
  [fnvar largs]
  (let [fn-meta (meta fnvar)
        params (first (:arglists fn-meta))
        kparams (map keyword params)
        parargs (zipmap kparams largs)
        enforced-args (run-aux fn-meta params parargs)]
    (if-let [errors (get-errors enforced-args)]
      ; uh oh, errors, throw exception
      ; TODO: clean this up - this should throw back the first error or return the errors...
      (throw+ {:type :enforce-error :message "problems in enforce" :errors errors})
      ; it's all good, call the function (but first "unzipmap" on the new args)
      (apply fnvar (map #(% enforced-args) kparams)))))
