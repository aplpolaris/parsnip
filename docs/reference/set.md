# Set Operations

When operating on value sets, Parsnip provides *computations* for sets of values, primarily to help with
aggregating and counting.

## Set Filters

No set filters (mapping sets to booleans) are provided yet.

## Set Computations { #set-compute }

### Counting

**Count** computations count the content of sets in various ways.

| Function | Description |
|----------|-------------|
| `Count: {}` | Count the number of elements of a set. |
| `CountDistinct: {}` | Count the number of distinct elements of a set. |
| `CountMissing: {}` | Count the number of missing (null) elements of a set. |
| `CountNonNull: {}` | Count the number of non-null elements of a set. |
| `CountNumeric: {}` | Count the number of finite, numeric elements of a set. |
| `CountValid: {}` | Count the number of valid elements (finite if a number, non-null otherwise) of a set. |

### Statistics

**Statistics** computations summarize the data. The precise numeric type returned depends on the input numeric types.
These functions throw a `NullPointerException` if asked to operate on a non-numeric value (although strings such as
`"1"` are fine). If asked to operate on an empty set, *mean* and *average* will return a `NaN` value.

| Function | Description |
|----------|-------------|
| `Stats: {}` | Compute summary statistics of a set of numbers (treated as floating-point numbers). |
| `IntStats: {}` | Compute summary statistics of a set of integers. |
| `Sum: {}` | Sum of numeric elements. |
| `Mean: {}` | Mean of numeric elements. |
| `Average: {}` | Average of numeric elements. |
| `Min: {}` | Min of numeric elements. |
| `Max: {}` | Max of numeric elements. |

## Set Transformations

No set transformations (mapping sets to sets) are provided yet.
