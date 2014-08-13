(ns enforcer.test.core
  (:require [clojure.test :refer [deftest is testing]]
            [mx.enforcer.core :refer :all]))

(defn coercer1 [param arg]
  arg)
(defn validator1 [param arg]
  arg)

(defn test-fn1
  {:enforcer-ns 'enforcer.test.core}
  [^{:coerce coercer1 :validate validator1} a
  ^{:coerce coercer1 :validate validator1} b
  ^{:coerce coercer1 :validate validator1} c]
  {:a a :b b :c c})

(deftest test-enforce
  (testing "test-enforce"
    (is (= (enforce #'test-fn1 {:a 1 :b 2 :c 3}) {:a 1 :b 2 :c 3}))))



(defn coercer2 [param arg]
  arg)
(defn validator2 [param arg]
  (if (< arg 5)
    arg
    (throw (Exception. "error msg"))))

(defn test-fn2
  {:enforcer-ns 'enforcer.test.core}
  [^{:coerce coercer2 :validate validator2} a
  ^{:coerce coercer2 :validate validator2} b
  ^{:coerce coercer2 :validate validator2} c]
  {:a a :b b :c c})

(deftest test-get-errors
  (testing "test-get-errors"
    (let [res (enforce #'test-fn2 {:a 1 :b 5 :c 3})]
      (is (= (:a res) 1))
      (is (= (nil? (:error (:b res))) false))
      (is (= (:c res) 3))

      (is (= (count (get-errors res)) 1)))))
