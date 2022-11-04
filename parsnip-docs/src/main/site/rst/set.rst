####################################
Set Operations
####################################
When operating on value sets, Parsnip provides *computations* for sets of values, primarily to help with aggregating and counting.

Set Filters
----------------------------
No set filters (mapping sets to booleans) are provided yet.

.. _set-compute:

Set Computations
-----------------------

Counting
************
**Count** computations count the content of sets in various ways.

.. function:: Count: {}

  Count the number of elements of a set.

.. function:: CountDistinct: {}

  Count the number of distinct elements of a set.

.. function:: CountMissing: {}

  Count the number of missing (null) elements of a set.

.. function:: CountNonNull: {}

  Count the number of non-null elements of a set.

.. function:: CountNumeric: {}

  Count the number of finite, numeric elements of a set.

.. function:: CountValid: {}

  Count the number of valid elements (finite if a number, non-null otherwise) of a set.

Statistics
************
**Statistics** computations summarize the data.
The precise numeric type returned depends on the input numeric types.
These functions throw a ``NullPointerException`` if asked to operate on a non-numeric value (although strings such as ``"1"`` are fine).
If asked to operate on an empty set, *mean* and *average* will return a ``NaN`` value.

.. function:: Stats: {}

  Compute summary statistics of a set of numbers (treated as floating-point numbers).

.. function:: IntStats: {}

  Compute summary statistics of a set of integers.

.. function:: Sum: {}

  Sum of numeric elements.

.. function:: Mean: {}

  Mean of numeric elements.

.. function:: Average: {}

  Average of numeric elements.

.. function:: Min: {}

  Min of numeric elements.

.. function:: Max: {}

  Max of numeric elements.

Set Transformations
----------------------------
No set transformations (mapping sets to sets) are provided yet.