(ns mx.enforcer.validation
  (:require [slingshot.slingshot :refer [try+]]))

;;; VALIDATION

(defn validate
  "Applies the `validate-fn` with the `arg` as argument. If `validate-fn` throws
  an exception, `on-validate-fail` will be called and supplied with the available
  information at the time."
  [validate-fn param arg on-validate-fail]
  (try+
    (validate-fn arg)
    (catch Object e
      (on-validate-fail e param arg))))

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
