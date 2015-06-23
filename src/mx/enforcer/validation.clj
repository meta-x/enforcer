(ns mx.enforcer.validation
  (:require [slingshot.slingshot :refer [try+ throw+]]))

;;; VALIDATION

(defn try-validate
  "Applies the `validate-fn` with the `arg` as argument. If `validate-fn` throws
  an exception, `on-validate-fail` will be called and supplied with the available
  information at the time."
  [validate-fn param arg on-validate-fail]
  (try+
    (validate-fn param arg) ; try to validate value
  (catch Object e ; uh oh, something happened
    (if on-validate-fail
      (on-validate-fail e param arg) ; there's an error handler, execute it
      (throw+ e))))) ; there's no error handler, so rethrow exception

(defn default-on-validate-fail
  [exception param arg]
  {
    :error
    {
      :msg (str "Failed to validate " param " with value " arg ". Exception: " exception)
      :param param
      :arg arg
      :exception (str exception)
    }
  })
