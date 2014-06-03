# enforcer

A library for coercion (or more correctly, type-casting) and validation of functions and their arguments. Designed use case is for Ring apps with the [`paths`](https://github.com/meta-x/paths) routing library.

ATTN: under development!

Please give feedback/suggestions/etc through github issues.



## Examples

* Check the project in `examples/enforcer_lib` for an example of a common clojure application using `enforcer` as a library. You can run the application by executing `lein run`.

* The folder `examples/enforcer_paths` has an example of `enforcer` as a middleware. Running `lein ring server` will launch a web server and open your browser at [http://localhost:3000](http://localhost:3000), showing you a series of links that should be self-explanatory.



## Installation

Add

```clojure
[enforcer "0.1.0-beta1"]
```

to your leiningen `:dependencies`.



## Usage

`enforcer` can be used as a common library in your clojure apps or, more specifically, as a ring middleware with [`paths`](https://github.com/meta-x/paths) or any compatible routing library.

The key concepts are:
* The target function and its arguments: these are the arguments that you want to coerce and/or validate
* The coercion/validation functions and error handlers: these are the functions that take care of executing the coercing and/or validation and that deal with something going wrong

This semi-legible image tries to explain how things are tied together
![How enforcer works](/doc/how-enforcer-works.jpg?raw=true)

Follow the white rabbit to learn more about how to use `enforcer`.

### 1. Require the library
Require what you wish to use. If you're only planning to use `enforcer` as a library in a common clojure application, you need only to require the `enforce` function or `enforce-all` for the rare situations where you might want to apply enforcement on multiple target functions.

If you're dealing with a ring application, you only need to require the middleware.

```clojure
(ns ...
  (:require [mx.enforcer.core :refer [enforce enforce-all]]
            [mx.enforce.middleware :refer [wrap-enforcer]])
  )
```

### 2. Create your coercion/validation functions and error handlers
The `coerce`/`validate` functions must take two parameters:
* `param`: the name of the argument that is being evaluated
* `arg`: the value of the argument that is being evaluated

```clojure
(defn custom-coercer [param arg]
  ...)

(defn custom-validator [param arg]
  ...)
```

In case of success these functions must return a single value - the `coerce` functions should return a casted value; the `validate` functions will return the (same) input value `arg`.
In case of error, the functions must throw an `exception`.

In alternative, you can have a do-it-all `enforce` function. The `enforce` function is for the cases where you'd rather have a single function where you deal with everything. The `enforce` function must have the same signature and return value as the `coerce`/`validate` functions.
```clojure
(defn custom-enforcer [param arg]
  ...)
```
In case of error, a map with a single key named `:error` must be returned. The value of the map does not have to follow any rule.

You _should_ also define your own error handlers (`:coerce-fail`/`:validate-fail`). There are two different "levels" of error handlers:
* parameter error handlers are set up at the parameter level and will handle errors when evaluating that specific parameter
* general error handlers are set up at the target function level and will work as a kind of try/catch all, i.e. if the parameter does not have an error handler configured, this one will be called (if defined)

Error handlers are functions that take three arguments:
* `exception`: the exception with the caused error
* `param`: the name of the argument that was being evaluated
* `arg`: the value of the argument that was being evaluated
and return a map with a single key named `:error` with any value.

In case of error while evaluating a target function's arguments, the values of all the `:error` keys will be aggregated and returned.

### 3. Define your target functions and the parameters you wish to cast & validate
After creating the functions that execute the coercion and validation, we must tell `enforcer` how to find these functions and what is the target function and the parameters to evaluate. This is done by attaching metadata keys to the target function and it's parameters.

```clojure
(defn ^{:enforcer-ns 'enforcer-paths.core :validate-fail custom-validate-fail :coerce-fail custom-coerce-fail} my-fn
  [^{:enforce custom-enforcer} p1
   ^{:coerce custom-coercer :validate custom-validator} p2]
   ...
  )
```
#### Target function level setup

[**required**] Add a `:enforcer-ns` key to the target function's metadata. The value of this key specifies the namespace under which the coercion/validation/error-handler functions are located.

[_optional_] You can define general error handlers for the case where coercion/validation fails. In case you don't specify argument specific error handlers, it will fall back to the general error handlers. In the case where no error-handler is defined, it will default to the library's own error handlers. These error handlers are specified with the keys `:coerce-fail` and `:validate-fail`.

#### Argument level setup

[_optional_] The coercion (`:coerce`) and validation (`:validate`) functions are optional.
In the case where a coercion function is not defined but a validation function is, the argument will passed as-is to the validation function.
The same is true in the case where there is a coercion function but not a validation function - the return value will be the coerced return value.

[_optional_] In the case where you prefer to just do it all in a single function, you can use the `:enforce` metadata key. The presence of this key takes precedence over `:coerce` and `validate`.

### 4a. Executing `enforcer`
With all set up, whenever you want to apply the enforcement, you just call `enforce`, passing the var of the target function e.g. `#'my-fn` and the list of arguments.
```clojure
  (enforce #'my-fn [1 2])
```
`enforce` will return a map that consists of param:value pairs (in this case `{:p1 1 p2 2}`).

TODO:
create an example using enforcer as a library
will everything work correctly? or will I need to create a function to return the right value to the caller?


### 4b. Using `enforcer` as a ring middleware
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

`wrap-enforcer`also takes an optional third argument that is an error handler for the middleware. The default error handler returns an http response with the 400 status code (bad request) and a JSON body with all the errors. To override this default behavior, implement a function that accepts a single argument that is a vector of errors. The structure of each error is however you defined your errors to be in your `enforcer` error handlers.



## How it Works

TODO: image of how `enforcer` works


## TODO / You can help by

- writing real tests

- improve doc

- ask questions, make suggestions, etc



## License

Copyright © 2014 Tony Tam

Released under the MIT license.
