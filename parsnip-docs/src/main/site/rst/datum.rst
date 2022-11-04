####################################
Datum Transformations
####################################
Recall that *datums* are maps (or dictionaries for python users) with string keys and arbitrary values. This is equivalent to
rows of data in a database, lines in a CSV file, etc.

.. _datum-filters:

Datum Filters
----------------------------

Filtering by Fields
*********************
A *compound field filter* called **DatumFieldFilter** filters datums based on expected values for individual fields.
It is configured as a key-value map where the keys are fields (or JSON pointers) and values may be values, lists of values, or other filter objects.

.. function:: DatumFieldFilter: { fieldPointer1: value/filter1, fieldPointer2: value/filter2, ... }

  Filter data by requiring fields to match given values or filters.

  :parameter: Key-value object where keys are either fields or *JSON pointers* to fields, and values are either scalar values (for exact matching),
    list values (for matching multiple values), or filter objects (for general matches).

The values for each field may be a scalar value, a list value, or an object:

- When the value is a simple value (string, number, or boolean), the filter looks for a match with the associated field.
  By default, the matching operation ignores types, i.e. the string `"10"` is considered the same as the integer `10` and the floating-point value `10.0`.
- When the value is a list, the filter looks for matches with any of the values in the list.
- When the value is an object, it is assumed to be a *value filter* and may be configured either with a single key indicating the filter type, and either a single value or a compound value describing parameters of the filter.

Here is an example with all three kinds of matching:

.. code-block:: yaml

  DatumFieldFilter:
    /messageBody/sensorId: 2
    /messageBody/vehicleId: [1, 2, 3]
    /messageBody/state:
      Gte: 40.0

Combination Tests
*********************
The following tests are either trivial (and provided for completeness) or operate with other filters as inputs.

.. function:: All: {}

  Matches all values (always returns true).

.. function:: None: {}

  Matches no values (always returns false).

.. function:: Not: filter

  Negates the given filter, e.g. ``Not: { DatumFieldFilter: { ... } }``

  :parameter: filter to negate

.. function:: And: [ filter1, filter2, ... ]

  Returns true if all provided filters match (or if list of filters is empty).

  :parameter: list of filters to combine

.. function:: Or: [ filter1, filter2, ... ]

  Returns true if any of the provided filters match (returns false if the list is empty).

  :parameter: list of filters to combine

.. _datum-compute:

Datum Computations
----------------------------
Datum computations convert an input datum to an output value (other than a datum).

Basic
**********
.. function:: Constant: x

  Return a fixed value no matter the input.

  :parameter: constant to return

.. function:: Field: "f"

  Return the value in the given field.

  :parameter: name of field to get value from

Conditionals
*************
Conditional functions calculate values differently depending on different conditions:

.. function:: Condition: [ { when: { field1: filter1, ... }, value: compute1 }, ... ]

  Return a different computed value depending on which filter matches first.

  :param when: A key-value object where keys are *field* names and values are ``ValueFilter`` objects.
  :param value: A ``DatumCompute`` object used to compute the return value when this condition is met.

**Examples:**

.. code-block:: yaml

    - Condition:
        - when: { /messageBody/state: { Lt: 20.0 } }
          value: { Constant: low }
        - when: { /messageBody/state: { Range: [20.0, 39.99] } }
          value: { Constant: med }
        - when: { /messageBody/state: { Gt: [40.0] } }
          value: { Constant: high }

Compound
***********
Compound computations put together multiple computations of different types.

.. function:: Chain: { from: compute1, process: [ compute2, compute3, ... ] }

  Apply a single datum computation, followed by multiple value computations, allowing a sequence of operations to be performed before obtaining a final result.

  :param from: A ``DatumCompute`` object that is applied first.
  :param process: Zero or more subsequent computations of type ``ValueCompute``.

**Examples:**

.. code-block:: yaml

  Chain:
    from:
      Field: a.capital
    process:
      - Add: 100.0
      - Divide: 2.0

Numbers
***********
Numeric computations can combine values from one or more input fields to produce a result.

.. function:: Calculate: "template expression"

  Compute a mathematical expression on the datum fields. Allows insertion of values using JSON pointer notation within braces.

  :parameter: template expression string, e.g. ``{a} + 2``, ``{\a\b} + {\c}``, etc.

For combining lists of numeric values from different fields, the following is an alternative:

.. function:: MathOp: { operator: XX, fields: [ f1, f2, ... ], ifInvalid: x }

  Apply the given numeric operation to the values in the given fields and return the result.

  :param operator: one of: ``ADD, SUBTRACT, NEGATE, MULTIPLY, DIVIDE, MIN, MAX, AVERAGE, STD_DEV`` (defaults to ``ADD``) for numeric values, or one of: ``EQUAL, NOT_EQUAL, GT, GTE, LT, LTE`` for boolean values
  :param fields: list of field names whose values are being combined (number of required fields and order differs based on operator)
  :param ifInvalid: (optional) value to return if there's an error in the calculation, e.g. some of the values are not numbers; defaults to null

Boolean-valued computations can combine values from one or more input fields to produce a result.

.. function:: CalculateBoolean: "template expression"

  Compute a boolean expression on the datum fields. Allows insertion of values using JSON pointer notation within braces.

  :parameter: template expression string, e.g. ``{a} or {b}``, ``{\a\b} or {\c}``, etc.
  
Strings
***********
A string template operation can be used to combine multiple fields into a single string.

.. function:: Template: "template string"

  Construct a string from a combination of field values using a template syntax, e.g. ``Template: "Door {/doorNumber} {/trigger}"`` (see below).

  :parameter: template string

A number of syntaxes are supported:

- a JSON pointer to a field, e.g. `/body/door/id`
- a semicolon-separated list of JSON pointers, e.g. `/path/1;/path/2`; the template will use the value of the first pointer it finds with content
- a JSON pointer *template*, e.g. ``http://{/server/name}/stuff/{/server/path}.png``; in this syntax, JSON pointers are used to insert
  values from the input value, and combined with the template value to produce a single string.

Structure
***********
.. function:: ToArray: bool

  Convert a datum to an array of values, throwing out the keys, e.g. ``{a:1, b:[2, 3]}`` converts to ``[1, [2, 3]]``

  :param flatten: if true, values are flattened (so ``{a:1, b:[2, 3]}`` converts to ``[1, 2, 3]``); otherwise values are left as-is

.. _datum-transform:

Datum Transformations
----------------------------
Datum transformations convert an input datum to an output datum.

Basic
**********
.. function:: Identity: {}

  Return the input value without making any changes.

.. function:: LogDatum: { level: "X" }

  Log and return the input datum without any other changes.

  :param level: log level string (one of ``SEVERE``, ``WARNING``, ``INFO``, ``CONFIG``, ``FINE``, ``FINER``, ``FINEST``)

*Currently `LogDatum` is maintained separately from `LogValue` but we plan to combine them.*

Field Operations
*********************
**Field Changes** operations work with fields in the datum.

.. function:: RemoveFields: [ f1, f2, ... ]

  Remove given fields from the datum and return the result.

  :parameter: list of field names to remove

.. function:: RetainFields: [ f1, f2, ... ]

  Retain given fields from the datum, removing everything else, and return the result.

  :parameter: list of field names to remove

.. function:: FlattenFields: [ f1, f2, ... ]

  Flatten nested fields inside the given list of fields, e.g. ``"f": { "a": 3 }`` flattens to ``"f.a": 3``.

  :parameter: list of field names to flatten

.. function:: Symmetry: { f1: f1s, f2: f2s, ... }

  Swap values in each provided pair of fields, e.g. ``Symmetry: { ip1: ip2, port1: port2 }``.

  :parameter: key-value map where keys and values are pairs of fields that should be simultaneously swapped.

Field Creation
*********************
The functions here compute values for target fields and either add the results to the existing datum or create entirely new results.

.. function:: Create: { "f1": compute1, "f2": compute2, ... }

  Return datum whose values are computed using the provided compute functions.

  :param: key-value map where keys are fields to return and values are ``ValueCompute`` objects

.. function:: Augment: { "f1": compute1, "f2": compute2, ... }

  Add new values to the input datum, using the given computations, and return the result.

  :param: key-value map where keys are fields to return and values are ``ValueCompute`` objects

Mapping and Conditional Operations
******************************************
Conditional operations have different values depending on whether the input datum matches different filters. Examples are shown below.

.. function:: Mapping: [ { when: { field1: filter1, ... }, put: datum }, ... ]

  Adds content to input datum for each matching filter and returns the augmented result.

  :param when: key-value object where keys are *field* names and values are ``ValueFilter`` objects.
  :param put: key-value object with content to be added to the datum if the filter passes.

.. function:: Change: { monitor: "pointerField", "groupBy": "pointerField",
    "whenChange": [ { from: x1, to: x2, put: datum }, ... ] }

  Monitors a single field for changes, adding content to the input datum when certain kinds of changes are made.

  :param monitor: a single field or JSON pointer being tracked for changes
  :param groupBy: (optional) field or JSON pointer; when checking for changes, a separate "last value" will be maintained for each value in this field, including null/missing values
  :param whenChange: (optional) list of specific content to add to input datum when changes are detected
  :param whenChange.from: (optional) value changing from; if null or omitted, tracks arbitrary values
  :param whenChange.to: (optional) value changing to; if null or omitted, tracks arbitrary values
  :param whenChange.put: (optional) key-value object with content to be added to the datum for this change condition

**Examples:**

.. code-block:: yaml

    - Mapping:
        - when: { /messageBody/state: { Lt: 20.0 } }
          put: { state: low }
        - when: { /messageBody/state: { Range: [20.0, 39.99] } }
          put: { state: med }
        - when: { /messageBody/state: { Gt: [40.0] } }
          put: { state: high }
    - Change:
        monitor: state
        groupBy: /messageBody/sensorId

Multi-Datum Transformations
----------------------------
Multi-datum transformations convert a single input datum to multiple output datums, i.e. a data set.

Structure
***********

.. function:: Fold: { fields: [ f1, f2, ... ], as: [ n1, n2 ] }

  Split a datum into multiple datums, one for each field given. The field name and value are encoded in new fields, for instance ``Fold: { fields: [ a, b ] }`` converts ``{a:1, b:2}`` to ``[{a:1, b:2, key:a, value:1}, {a:1, b:2, key:b, value:2}]``.

  :param fields: list of fields to pull out as separate key-value pairs
  :param as: (optional) list of two names to use for key and value fields in the result; defaults to ``[ key, value ]``

.. function:: Flatten: { fields: [ f1, f2, ... ], as: [ n1, n2, ... ] }

  Split a datum into several datums by extracting values from lists for the given fields, for instance ``Flatten: { fields: [a] }`` converts ``{a: [1,2]}`` to ``[{a:1}, {a:2}]``

  :param fields: list of fields whose values should be flattened into multiple datums
  :param as: (optional) list of field names to use in the result; if omitted, uses the source field names