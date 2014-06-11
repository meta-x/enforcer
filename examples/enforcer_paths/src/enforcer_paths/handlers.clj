(ns enforcer-paths.handlers
  (:require [ring.util.response :refer [response]]
            [enforcer-paths.enforcement :refer [custom-validate-fail custom-coerce-fail custom-enforcer custom-coercer custom-validator custom-enforcer-wildcards]])
  )

;;; HANDLERS

(defn index []
  (response "<div>hi</div>
            <div>you should be running this with an eye on the <a href='https://github.com/meta-x/enforcer/tree/master/examples/enforcer_paths/src/enforcer_paths'>example's source code</a>.</div>
             <a href='/with/args?p1=1&amp;p2=2&amp;p3=0'>handler that takes 3 different arguments</a><br/>
             <a href='/with/no/args'>handler that takes no arguments</a><br/>
             <a href='/this/accepts/anything/yeah'>wildcard example</a><br/>
            "))


(defn ^{:enforcer-ns 'enforcer-paths.enforcement :validate-fail custom-validate-fail :coerce-fail custom-coerce-fail} handler-with-args
  [^{:enforce custom-enforcer} p1
   ^{:coerce custom-coercer :coerce-fail custom-coercion-error-handler :validate custom-validator :validate-fail custom-validation-error-handler} p2
   ^{:coerce custom-coercer :validate custom-validator} p3]
  (println "--- handler-with-args")
  (println p1 (type p1))
  (println p2 (type p2))
  (println p3 (type p3))
  (response (str p1 "\n" p2 "\n" p3)))

(defn handler-with-no-args
  []
  (println "--- handler-with-no-args")
  (response "no args"))

(defn ^{:enforcer-ns 'enforcer-paths.enforcement} handler-wildcard
  [^{:enforce custom-enforcer-wildcards} wildcards]
  (println "--- handler-wildcard")
  (response wildcards))
