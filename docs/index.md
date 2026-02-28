# Parsnip

*Parsnip* provides an easy, flexible, and highly-configurable way to manipulate data encoded in *JSON*.
It has several goals:

- Be able to manipulate any combination of list, map, and primitive objects, i.e. anything that looks like a JSON object.
- Reference content in data using [JSON pointer notation](https://tools.ietf.org/html/rfc6901).
- Provide interfaces and common implementations of filters, transformations, aggregations, and other operations on these in-memory data structures.
- Provide flexibility when comparing values of different types (e.g. the integer `10` vs. the string `"10"`).
- Be able to save/load these implementations in JSON or YAML, so they may be configured by end-users without writing code.
- Provide basic tools for putting these operations together into *ETL (extract-transform-load)* pipelines.

Parsnip is useful in any environment that needs to make these kinds of transformations, and is particularly useful because
editing data transformations *requires no code modifications* — all ETLs and transformations can be configured entirely
within JSON/YAML files.

## Modules

- **parsnip-types** [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.googlecode.blaisemath/parsnip-types/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.googlecode.blaisemath/parsnip-types) — *general utilities, mostly for type deserialization*
- **parsnip** [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.googlecode.blaisemath/parsnip/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.googlecode.blaisemath/parsnip) — *primary library for data manipulation and ETL*

## Parsnip Data Types

*Parsnip* provides tools for working with four kinds of data:

| Type | Description |
|------|-------------|
| **value** | an arbitrary value (often a scalar or primitive, but may not be) |
| **set** | an ordered collection of values |
| **datum** | a map (or dictionary) with string keys and arbitrary values |
| **dataset** | an ordered collection of datums |

For collections, Parsnip makes the additional distinction between *sets* (which have a fixed size) and *sequences*
(which can be iterated over but may not have a known size).

## Parsnip Data Operations

There are many ways to operate on the above data types. For convenience, the Parsnip APIs (both the code API and the
JSON/YAML API) are organized around three primary groups of operations:

| Operation | Description |
|-----------|-------------|
| **filters** | take one of the above types as input and produce a boolean (true/false) result |
| **computations** | take one of the above types as input and return a value |
| **transformations** | a specific version of a computation that returns the same type as the input |

A final special case is a *multi-transformation*, which takes one of the above types as an input and returns multiple
instances as an output.
