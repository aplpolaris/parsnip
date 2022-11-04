####################################
Introduction
####################################
*Parsnip* provides an easy, flexible, and highly-configurable way to manipulate data. It has several goals:

- Be able to manipulate any combination of list, map, and primitive objects, i.e. anything that looks like a JSON object.
- Reference content in data using `JSON pointer notation <https://tools.ietf.org/html/rfc6901>`_.
- Provide interfaces and common implementations of filters, transformations, aggregations, and other operations on these in-memory data structures.
- Provide flexibility when comparing values of different types (e.g. the integer ``10`` vs. the string ``"10"``).
- Be able to save/load these implementations in JSON or YAML, so they may be configured by end-users without writing code.
- Provide basic tools for putting these operations together into *ETL (extract-transform-load)* pipelines.

Parsnip is useful in any environment that needs to make these kinds of transformations, and is particularly useful because
editing data transformations *requires no code modifications* -- all ETLs and transformations can be configured entirely
within JSON/YAML files.

Summary of Parsnip Data Concepts
---------------------------------

Parsnip Data Types
*********************
*Parsnip* provides tools for working with four kinds of data:

value
  an arbitrary value (often a scalar or primitive, but may not be)
set
  an ordered collection of values
datum
  a map (or dictionary) with string keys and arbitrary values
dataset
  an ordered collection of datums

For collections, Parsnip makes the additional distinction between *sets* (which have a fixed size) and *sequences*
(which can be iterated over but may not have a known size).

Parsnip Data Operations
************************
There are many ways to operate on the above data types. For convenience, the Parsnip APIs (both the code API and the JSON/YAML API)
are organized around three primary groups of operations:

filters
  operations that take one of the above types as input and produce a boolean (true/false) result
computations
  operations that take one of the above types as input and return a value
transformations
  specific version of a computation that returns the same type as the input

A final special case that is used in some cases is a *multi-transformation*, which in our case takes one of the above types
as an input and returns multiple instances as an output.

Notes on JSON and YAML
---------------------------------
Examples using both *JSON* and *YAML* formats are provided throughout this documentation, although we prefer *YAML* where possible.
See `https://yaml.org/ <https://yaml.org/>`_ for an introduction to YAML and `https://www.json.org/ <https://www.json.org/>`_ for an introduction to JSON.
Where possible, we provide examples in *YAML*, where nested content as shown as indented lines (preferred for *YAML*) rather than with braces (required for *JSON*).

The two representations are equivalent and easily interchanged, as in the following examples:

.. code-block:: yaml

  Condition:
    - when:
      x:
        Gte: 5
      value:
        Field: a
    - when:
      x:
        Lt 5
      value:
        Field: b

.. code-block:: json

  { "Condition": [
    {
      "when": { "x": { "Gte": 5 } },
      "value": { "Field": "a" }
    }, {
      "when": { "x": { "Lt": 5 } },
      "value": { "Field": "b" }
    }
  ] }

Since *JSON* is valid *YAML*, sometimes we use a hybrid approach to balance readability and space:

.. code-block:: yaml

  Condition:
    - when: { x: { Gte: 5 }, value: { Field: a } }
    - when: { x: { Lt: 5 }, value: { Field: b } }

For the documentation of specific *Parsnip* filter and function formats, the function parameters will be given in a single line, as follows:

.. function:: FlattenList: { as: "field", index: "iField" }

When adding this function to a *YAML* file, the arguments could be expressed either inline or on separate lines:

.. code-block:: yaml

  FlattenList: { as: "value", index: "index" }

.. code-block:: yaml

  FlattenList:
    as: value
    index: index

Functions that do not have parameters are written in *YAML* with empty braces (so they are decoded as functions rather than strings):

.. function:: Count: {}