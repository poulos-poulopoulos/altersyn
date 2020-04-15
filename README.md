# altersyn

Altersyn is a library that provides macros which define some ALTERnative SYNtaxes for Clojure. These syntaxes use different representations for lists with the main purpose of enabling writing various combinations of forms (function/macro applications, special forms) in ways that mimic some well-known notations and in some cases reduce the nesting or the number of parentheses.

## `trama` and `depar`

The `trama` macro defines a syntax that resembles TRAditional MAthematical notation for function application. Here a form is represented by an operator followed by enclosed-in-parentheses operands.

The `depar` macro defines a somewhat DEPARenthesized syntax that mimics common ways of writing function applications in Haskell. Here a `.` separator is used between elements of a vector literal, operands and top-level forms. No-separator works as a high-priority left-associative form-constructor and a `$` separator works as a low-priority right-associative form-constructor, with `.` having even lower priority. To use the `.` (member access) special form one must replace `.` with `>>` and for the related `..` macro one must write `>>>`. There is also a variant of `depar`, named `depar+`, which allows omitting whitespace around `.` and `$` in some cases, by splitting Clojure-code symbols containing them.

When there is a set/map literal inside code following these syntaxes, the code inside it must follow Clojure syntax. Using reader macros inside these syntaxes is not correct in general (although there are some unproblematic cases). To know exactly how these macros work one has to read the code, as there is currently no other documentation.

## examples

The following are syntactically equivalent.

```
(trama f(x g(y h(z))))
(depar f $ (x . g) $ (y . h) z)
(->> z h (g y) (f x))
```

They all expand to `(f x (g y (h z)))`.

The following are syntactically equivalent.

```
(trama f(x)(g(y)(h(z))))
(depar f x $ g y $ h z)
(->> z h ((g y)) ((f x)))
```

They all expand to `((f x) ((g y) (h z)))`.
