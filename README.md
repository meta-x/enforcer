# enforcer

A library for coercion (or more correctly, type-casting) and validation of functions and their arguments. Designed use case is for Ring apps with the `[paths](https://github.com/meta-x/paths)` routing library.

ATTN: under development!

Please give feedback/suggestions/etc through github issues.



## Example

TODO: to be finished


## Installation

TODO: to be described

## Usage

`enforcer` can be used as a common library in your clojure apps or, more specifically, as a ring middleware with `[paths](https://github.com/meta-x/paths)` or any compatible routing library.

In order to use `enforcer`, you must follow the following steps.

1. Require the library
Require what you wish to use. If you're only planning on using `enforcer` as a library in a common clojure application, you need only to require the `enforce` function or `enforce-all` for the rare situations where you may want to apply enforcement on multiple target functions.
If you're dealing with a ring application, you only need to require the middleware.
```clojure
(ns ...
  (:require [mx.enforcer.core :refer [enforce enforce-all]]
            [mx.enforce.middleware :refer [wrap-enforcer]])
  )
```

2. Create your coercion/validation functions using the following signature:
```clojure
(defn custom-coercer [param arg]
  ...)

(defn custom-validator [param arg]
  ...)
```
In case of success these functions must return a single value: the `coercion` functions should return a casted value; the `validation` functions will return the (same) input value `arg`. In case of error, the functions must throw an `exception`.

Alternative, you can have a do-it-all `enforce` function with the following signature:
```clojure
(defn custom-enforcer [param arg on-coerce-fail on-validate-fail]
  ...)
```
In case of success, return the value of the argument. In case of error, a map with a single key named `:error` must be returned. The value of the map does not have to follow any rule.

3. Define your target functions and the parameters you wish to cast & validate in the following way:
```clojure
(defn ^{:enforcer-ns 'enforcer-paths.core :validate-fail custom-validate-fail :coerce-fail custom-coerce-fail} handler-with-args
  [^{:enforce custom-enforcer} p1
   ^{:coerce custom-coercer :validate custom-validator} p2]
  (println "--- handler-with-args")
  (println p1)
  (println p2)
  (response (str p1 "\n" p2)))
```
-target function level setup-
**required* Add a metadata key to the target function and name it `:enforcer-ns`. The value of this key specifies the namespace under which the coercion/validation/error-handler functions are located.

_optional_ You can define general error handlers for the case where coercion/validation fails. In case you don't specify argument specific error handlers, it will fall back to the general error handlers. In the case where no error-handler is defined, it will default to the library's own error handlers.

-argument level setup-
_optional_ The coercion and validation functions are optional.
In the case where a coercion function is not defined but a validation function is, the argument will passed as-is to the validation function.
The same is true in the case where there is a coercion function but not a validation function - the return value will be the coerced return value.

_optional_ In the case where you prefer to just do it all in a single function, you can use the `:enforce` metadata key. The presence of this key takes precedence over `:coerce` and `validate`.

4a. Execute `enforcer`
With all set up, whenever you want to apply the enforcement, you just call `enforce`, passing the var of the target function e.g. `#'my-fn` and the list of arguments.
```clojure

```
TODO:
create an example using enforcer as a library
will everything work correctly? or will I need to create a function to return the right value to the caller?


4b. `enforcer` as a ring middleware
Using `enforcer` as a middleware to your ring app might require some additional setup, depending on your routing library. If you are using `paths`, you must have manually created a `routes-tree` and use `paths`' `bind-query-routes` function when calling `wrap-enforcer`, i.e.
```clojure
(wrap-enforcer (bind-query-routes routes-tree))
```
If you are **not** using `paths`, then you must tell `enforcer` how to find the handler function for any given request. This means creating a function with the following signature and return value:
```clojure
(fn [request]
  ...
  ; returns the var of the handler function of the request
  )
```
The return value is the #'target-function from which `enforcer` will consult the metadata and find the coercion and validation functions to apply.

In order to achieve this using a different routing library, you might need to add some kind of dispatching function to handle your requests. E.g. in compojure, this might mean something like
```clojure
TODO: compojure handler dispatcher
```



## How it Works

TODO: image of how `enforcer` works


## TODO / You can help by

- writing real tests

- improve doc

- ask questions, make suggestions, etc



## License

Copyright © 2014 Tony Tam

Released under the MIT license.
