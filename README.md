Distributable wrapper for Z3 java api
=====================================

Attempt at improving portability of the z3 java api. This
project is heavily inspired by previous work in
[ScalaZ3](https://github.com/epfl-lara/ScalaZ3).

Building Z3jar
==============

Depends on Java and SBT 0.13.x.

1. Start by cloning the [Z3 source repository](https://github.com/Z3Prover/z3)
into z3/ (this will be the default location if cloning from repository base).

2. In order to build z3 and package all relevant sources into a single jar,
simply call `sbt package`. The resulting jar (for scala 2.11) can be found at
```
target/scala-2.11/z3jar_2.11-1.0.jar
```







