(ns mx.enforcer.coerce-utils
  )

; coercing helpers (i.e. common coercing functions)

(defn to-str [v]
  (str v))

(defn to-bool [v]
  (Boolean/parseBoolean v))

(defn to-int [v]
  (Integer/parseInt v))

(defn to-long [v]
  (Long/parseLong v))

(defn to-float [v]
  (Float/parseFloat v))

(defn to-double [v]
  (Double/parseDouble v))

; TODO: other numeric values (decimal, money, ?)

(defn to-datetime [v]
  ; TODO: use jodatime or https://github.com/mbossenbroek/simple-time
  )

; TODO: streams?

; TODO: collections? - doubt it
