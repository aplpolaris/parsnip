####################################
Value Operations
####################################
When operating on values, Parsnip provides *filters* and *computations* for common value types.

.. _value-filters:

Value Filters
---------------

Equality Tests
**********************
**Equality Matches** check the current value against another. Comparisons are flexible with types, e.g. `10` is considered the same as `10.0` or the string `"10"`.

.. function:: IsNull: {}

  Test if the given value is null.

.. function:: IsNotNull: {}

  Test if the given value is not null.

.. function:: IsEmpty: {}

  Test if the given value is null or an empty string.

.. function:: IsNotEmpty: {}

  Test if the given value is not null or an empty string.

.. function:: Equal: x

  Test equality with the given value.

  :parameter: value for comparison

.. function:: NotEqual: x

  Test inequality with the given value.

  :parameter: value for comparison

.. function:: OneOf: [ x1, x2, x3 ]

  Test whether value matches one of the provided elements.

  :parameter: list of values for comparison

Comparison Tests
**********************
**Comparison Matches** check that a value is less than or greater than a given value.
Any value that is ``Comparable`` is permitted, e.g. string values are allowed and will lead to string comparisons.
Non-comparable values are also permitted, but will be compared using their ``toString`` representation.

.. function:: Gt: x

  Test if value is greater than parameter value.

  :parameter: value for comparison

.. function:: Gte: x

  Test if value is greater than or equal to parameter value.

  :parameter: value for comparison

.. function:: Lt: x

  Test if value is less than parameter value.

  :parameter: value for comparison

.. function:: Lte: x

  Test if value is less than or equal to parameter value.

  :parameter: value for comparison

.. function:: Range: [ min, max ]

  Test if value is greater than or equal to min value and less than or equal to max value.

  :parameter: list of two elements with min and max value

  Test if value is less than or equal to parameter value.

String Tests
***************
Strings can be filtered using the functions below.

.. function:: Contains: "s"

  Tests if value contains the parameter string (case-sensitive).

  :parameter: substring to look for

.. function:: StartsWith: "s"

  Tests if value starts with the parameter string (case-sensitive).

  :parameter: substring to look for

.. function:: EndsWith: "s"

  Tests if value ends with the parameter string (case-sensitive).

  :parameter: substring to look for

.. function:: Matches: "regex"

  Tests if value contains a substring that matches the given regex.

  :parameter: regex for substring matching

.. function:: ContainsMatch: "regex"

  Tests if value matches the given regex.

  :parameter: regex for entire string matching

Date/Time Tests
*****************
*These are TBD*

IP/CIDR Tests
***************
.. function:: IsIp: {}

  Test whether format of string matches an IP, e.g. ``"192.168.0.1"``.

.. function:: IsCidr: {}

  Test whether format of string matches a CIDR, e.g. ``"192.168.0.0/16"``.

.. function:: IpContainedIn: "cidr"

  Test whether the value is an IP contained in the parameter CIDR. Returns false if the value is not an IP string.

  :parameter: CIDR string, e.g. ``"192.168.0.0/16"``

.. function:: CidrContains: "ip"

  Test whether the value is a CIDR that contains the parameter IP. Returns false if the value is not a CIDR string.

  :parameter: IP string, e.g. ``"192.168.0.1"``

Combination Tests
*********************
The following tests are either trivial (and provided for completeness) or operate with other filters as inputs.

.. function:: All: {}

  Matches all values (always returns true).

.. function:: None: {}

  Matches no values (always returns false).

.. function:: Not: filter

  Negates the given filter, e.g. ``Not: { OneOf: [1, 2, 3] }``

  :parameter: filter to negate

.. function:: And: [ filter1, filter2, ... ]

  Returns true if all provided filters match (or if list of filters is empty).

  :parameter: list of filters to combine

.. function:: Or: [ filter1, filter2, ... ]

  Returns true if any of the provided filters match (returns false if the list is empty).

  :parameter: list of filters to combine

.. _value-compute:

Value Computations
--------------------
**Value Computations** have a single value as an input, and produce a single value as output.

Basic
**********
.. function:: Constant: x

  Return a fixed value no matter the input.

  :parameter: constant to return

.. function:: Identity: {}

  Return the input value without making any changes.

.. function:: LogValue: { level: "X" }

  Log and return the input value without any other changes.

  :param level: log level string (one of ``SEVERE``, ``WARNING``, ``INFO``, ``CONFIG``, ``FINE``, ``FINER``, ``FINEST``)

.. function:: Lookup: { table: { ... }, ifNull: x, caseSensitive: bool }

  Return a value based on a lookup table.
  Input values are checked against the lookup table by comparing string values, trimmed of spaces before/after characters, and checking for either exact or case-insensitive matches.
  Example: ``Lookup: { table: { "0": "zero", "1": "one", "2": "two" }, ifNull: "something else", caseSensitive: false }``.

  :param table: the lookup table, with string keys (the value to look for) and arbitrary values (the result if a match is found)
  :param ifNull: (optional) value to return if no match is found; defaults to ``null``
  :param caseSensitive: (optional) whether matches should be case-sensitive; if false, permits case-insensitive value matching; defaults to false

Numbers
***********
**Numeric computations** perform mathematical operations on a value. These functions return ``null`` for invalid inputs.

.. function:: Add: n

  Return value plus *n*.

  :parameter: number to add

.. function:: Subtract: n

  Return value minus *n*.

  :parameter: number to subtract


.. function:: Multiply: n

  Return value times *n*.

  :parameter: number to multiply


.. function:: Divide: n

  Return value divided by *n*.

  :parameter: number to divide

.. function:: Linear: { domain: [ dmin, dmax ], range: [ rmin, rmax ] }

  Apply a linear transformation to the (numeric) input, so that *dmin* maps to *rmin* and *dmax* maps to *rmax*.

  :param domain: list of min and max value for the domain (inputs)
  :param range: list of min and max value for the range (outputs)

Strings
************
**String computations** operate on string values.

.. function:: RegexCapture: { regex: "regex", as: [ f1, f2, ... ] }

  Capture values from an input string using the given regex's capture groups. For instance, ``RegexCapture: { regex: "([0-9]*)\\.([0-9]*)", as: [x, y] }`` converts ``"123.456"`` to ``{x:123, y:456}``. Returns null for invalid regular expressions, or when the input doesn't match.

  :param regex: the regular expression, with parentheticals indicating capture groups
  :param as: list of field names for the capture groups; use an empty string to skip the field

Structure
************
**Structural computations** alter the structure of an input in some way.

.. function:: FlattenList: { as: f1, index: f2 }

  Convert each element of a list to a datum with an additional index field, e.g. ``[1, 2, 3]`` might be converted to ``{a:1, i:0}, {a:2, i:1}, {a:3, i:2}``. (If the input is not a list, it is left unchanged.)

  :param as: name of field name used for values in the list
  :param index: name of field name used for index in the list

.. function:: FlattenMatrix: { as: f1, index1: f2, index2: f3 }

  Convert each element of a matrix to a datum with additional row and column fields, e.g. ``[[1, 0], [2, 3]]`` might be converted to ``{a:1, i:0, j:0}, {a:0, i:0, j:1}, ...``. (If the input is not a matrix, it is left unchanged.)

  :param as: name of field name used for values in the list
  :param index1: name of field name used for row index in the list
  :param index2: name of field name used for column index in the list

.. function:: OneHot: [ value1, value2, ... ]

  Convert an "enum" value into an array of 0 or 1 depending on the value, e.g. ``OneHot: [ a, b, c ]`` would map ``"b"`` to ``[0, 1, 0]``.
  If there are no matching values, the result will contain all zeros.

  :parameter: list of possible enum values

Type Conversions
*****************
A number of **type conversions** are provided to convert values from one type to another, sometimes with additional parameters so user can set precise formats.
The ``As`` function attempts a direct conversion, while the ``Decode`` function specifies a specific conversion and allows more precise (and often faster) conversions.

.. function:: As: type

  Convert value to target (Java) type, e.g. ``As: Double`` or ``As: LocalDateTime``, using best available conversion. (This can handle some date/time string formats but not all.)

  :parameter: Java type; should be fully-qualified unless in the ``java.util``, ``java.lang``, or ``java.time`` packages.

.. function:: Decode: decoder

  Convert value to target type, using a custom decoder rather than attempting conversion directly.

  :parameter: decoder type string (one of ``NULL, STRING, BOOLEAN, LONG, INTEGER, SHORT, BYTE, DOUBLE, FLOAT, IP_ADDRESS, DOMAIN_NAME, HEX_STRING, LIST, DATE_TIME, DATE, TIME, EPOCH``);
    may also be a *type-parameter* object such as ``InstantDecoder: "yyyy-MM-dd'T'HH:mm:ss"`` (converting string with given format to an ``Instant`` type)
    or ``InstantEpochDecoder: "yyyy-MM-dd'T'HH:mm:ss"`` (converting string with given format to a ``long`` representing the epoch timestamp)

.. function:: DecodeIntant: date_time_foramt

  Convert value to an ``Instant`` using the given format string, e.g. ``"yyyy-MM-dd'T'HH:mm:ss"``.

  :parameter: date/time format string

Value Transformations
----------------------
Value transformations are considered the same as value computations.