(ns mx.enforcer.coercion
  (:require [slingshot.slingshot :refer [try+]]))

;;; "COERCION"

(defn coerce
  "Applies the `coerce-fn` with the `arg` as argument. If `coerce-fn` throws
  an exception, `on-coerce-fail` will be called and supplied with the available
  information at the time."
  [coerce-fn param arg on-coerce-fail]
  (try+
    (coerce-fn arg)
    (catch Object e
      (on-coerce-fail e param arg))))

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
