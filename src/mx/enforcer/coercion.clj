(ns mx.enforcer.coercion
  (:require [slingshot.slingshot :refer [try+ throw+]]))

;;; "COERCION"

(defn try-coerce
  "Applies the `coerce-fn` with the `arg` as argument. If `coerce-fn` throws
  an exception, `on-coerce-fail` will be called and supplied with the available
  information at the time."
  [coerce-fn param arg on-coerce-fail]
  (try+
    (coerce-fn param arg) ; try to coerce value
  (catch Object e ; uh oh, something happened
    (if on-coerce-fail
      (on-coerce-fail e param arg) ; there's an error handler, execute it
      (throw+ e))))) ; there's no error handler, so rethrow exception

(defn default-on-coerce-fail
  [exception param arg]
  {
    :error
    {
      :msg (str "Failed to cast " param " with value " arg ". Exception: " exception)
      :param param
      :arg arg
      :exception (str exception)
    }
  })
