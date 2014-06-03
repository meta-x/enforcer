(ns enforcer-paths.enforcement
  )

;;; ENFORCER

; general error handlers

(defn custom-validate-fail [exception param arg]
  (println "custom-validation-fail")
  {:error {
    :msg (str "1. " param " is not valid.")
    :param param
    :arg arg
    :exception (str exception)
  }})

(defn custom-coerce-fail [exception param arg]
  (println "custom-coerce-fail")
  {:error {
    :msg (str "1. " param " could not be cast.")
    :param param
    :arg arg
    :exception (str exception)
  }})

; enforcer/coercer/validator

(defn custom-enforcer [param arg]
  (println "custom-enforcer")
  (println param "\t" arg)
  (println arg)
  (if (nil? arg)
    {:error {
      :msg (str param " missing.")
      :param param
      :arg arg
      ;:exception
    }}
    arg))

(defn custom-enforcer-wildcards [param arg]
  (println "custom-enforcer")
  (println param "\t" arg)
  (println arg)
  (if (nil? arg)
    {:error {
      :msg (str param " missing.")
      :param param
      :arg arg
      ;:exception
    }}
    arg))

(defn custom-coercer [param arg]
  (println "custom-coercer")
  (println arg)
  (Integer/parseInt arg))

(defn custom-validator [param arg]
  (println "custom-validator")
  (println arg)
  (if (> arg 5)
    (throw (Exception. (str param " can't be > than 5.")))
    arg))

; argument error handlers

(defn custom-coercion-error-handler [exception param arg]
  (println "custom-coercion-error-handler")
  {:error {
    :msg (str "2. " param " is not valid.")
    :param param
    :arg arg
    :exception (str exception)
  }})

(defn custom-validation-error-handler [exception param arg]
  (println "custom-validation-error-handler")
  {:error {
    :msg (str "2. " param " could not be cast.")
    :param param
    :arg arg
    :exception (str exception)
  }})
