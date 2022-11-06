# Overview

*Parsnip* provides an easy, flexible, and highly-configurable way to manipulate data encoded in *JSON*. It has several goals:

- Be able to manipulate any combination of list, map, and primitive objects, i.e. anything that looks like a JSON object.
- Reference content in data using `JSON pointer notation <https://tools.ietf.org/html/rfc6901>`_.
- Provide interfaces and common implementations of filters, transformations, aggregations, and other operations on these in-memory data structures.
- Provide flexibility when comparing values of different types (e.g. the integer ``10`` vs. the string ``"10"``).
- Be able to save/load these implementations in JSON or YAML, so they may be configured by end-users without writing code.
- Provide basic tools for putting these operations together into *ETL (extract-transform-load)* pipelines.

Parsnip is useful in any environment that needs to make these kinds of transformations, and is particularly useful because
editing data transformations *requires no code modifications* -- all ETLs and transformations can be configured entirely
within JSON/YAML files.
