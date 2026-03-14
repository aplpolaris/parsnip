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

## Modules

- **parsnip-types** [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.googlecode.blaisemath/parsnip-types/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.googlecode.blaisemath/parsnip-types) *general utilities, mostly for type deserialization*
- **parsnip** [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.googlecode.blaisemath/parsnip/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.googlecode.blaisemath/parsnip) *primary library for data manipulation and ETL*

## Documentation

Complete documentation is available at **[aplpolaris.github.io/parsnip](https://aplpolaris.github.io/parsnip/)** including:

- **[User Guide](https://aplpolaris.github.io/parsnip/user-guide/)** - Comprehensive guide to using Parsnip for data manipulation and ETL
- **[API Reference](https://aplpolaris.github.io/parsnip/api/)** - Complete API documentation for all classes and methods

For local development and documentation setup instructions, see [DOCS_SETUP.md](DOCS_SETUP.md).
