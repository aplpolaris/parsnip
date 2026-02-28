# Value Operations

When operating on values, Parsnip provides *filters* and *computations* for common value types.

## Value Filters { #value-filters }

### Equality Tests

**Equality Matches** check the current value against another. Comparisons are flexible with types,
e.g. `10` is considered the same as `10.0` or the string `"10"`.

| Function | Description |
|----------|-------------|
| `IsNull: {}` | Test if the given value is null. |
| `IsNotNull: {}` | Test if the given value is not null. |
| `IsEmpty: {}` | Test if the given value is null or an empty string. |
| `IsNotEmpty: {}` | Test if the given value is not null or an empty string. |
| `Equal: x` | Test equality with the given value. |
| `NotEqual: x` | Test inequality with the given value. |
| `OneOf: [ x1, x2, x3 ]` | Test whether value matches one of the provided elements. |

### Comparison Tests

**Comparison Matches** check that a value is less than or greater than a given value.
Any value that is `Comparable` is permitted, e.g. string values are allowed and will lead to string comparisons.
Non-comparable values are also permitted, but will be compared using their `toString` representation.

| Function | Description |
|----------|-------------|
| `Gt: x` | Test if value is greater than parameter value. |
| `Gte: x` | Test if value is greater than or equal to parameter value. |
| `Lt: x` | Test if value is less than parameter value. |
| `Lte: x` | Test if value is less than or equal to parameter value. |
| `Range: [ min, max ]` | Test if value is between min and max (inclusive). |

### String Tests

| Function | Description |
|----------|-------------|
| `Contains: "s"` | Tests if value contains the parameter string (case-sensitive). |
| `StartsWith: "s"` | Tests if value starts with the parameter string (case-sensitive). |
| `EndsWith: "s"` | Tests if value ends with the parameter string (case-sensitive). |
| `Matches: "regex"` | Tests if value contains a substring matching the given regex. |
| `ContainsMatch: "regex"` | Tests if value matches the given regex entirely. |

### IP/CIDR Tests

| Function | Description |
|----------|-------------|
| `IsIp: {}` | Test whether format of string matches an IP, e.g. `"192.168.0.1"`. |
| `IsCidr: {}` | Test whether format of string matches a CIDR, e.g. `"192.168.0.0/16"`. |
| `IpContainedIn: "cidr"` | Test whether the value is an IP contained in the parameter CIDR. |
| `CidrContains: "ip"` | Test whether the value is a CIDR that contains the parameter IP. |

### Combination Tests

| Function | Description |
|----------|-------------|
| `All: {}` | Matches all values (always returns true). |
| `None: {}` | Matches no values (always returns false). |
| `Not: filter` | Negates the given filter, e.g. `Not: { OneOf: [1, 2, 3] }`. |
| `And: [ filter1, filter2, ... ]` | Returns true if all provided filters match. |
| `Or: [ filter1, filter2, ... ]` | Returns true if any of the provided filters match. |

## Value Computations { #value-compute }

**Value Computations** have a single value as an input, and produce a single value as output.

### Basic

| Function | Description |
|----------|-------------|
| `Constant: x` | Return a fixed value no matter the input. |
| `Identity: {}` | Return the input value without making any changes. |
| `LogValue: { level: "X" }` | Log and return the input value. Level is one of `SEVERE`, `WARNING`, `INFO`, `CONFIG`, `FINE`, `FINER`, `FINEST`. |
| `Lookup: { table: { ... }, ifNull: x, caseSensitive: bool }` | Return a value based on a lookup table. |

**Example — Lookup:**

```yaml
Lookup:
  table:
    "0": "zero"
    "1": "one"
    "2": "two"
  ifNull: "something else"
  caseSensitive: false
```

### Numbers

**Numeric computations** perform mathematical operations on a value. These functions return `null` for invalid inputs.

| Function | Description |
|----------|-------------|
| `Add: n` | Return value plus *n*. |
| `Subtract: n` | Return value minus *n*. |
| `Multiply: n` | Return value times *n*. |
| `Divide: n` | Return value divided by *n*. |
| `Linear: { domain: [ dmin, dmax ], range: [ rmin, rmax ] }` | Apply a linear transformation mapping dmin→rmin and dmax→rmax. |

### Strings

| Function | Description |
|----------|-------------|
| `RegexCapture: { regex: "regex", as: [ f1, f2, ... ] }` | Capture values from an input string using regex capture groups. |

**Example — RegexCapture:**

```yaml
# Converts "123.456" to { x: 123, y: 456 }
RegexCapture:
  regex: "([0-9]*)\\.([0-9]*)"
  as: [x, y]
```

### Structure

| Function | Description |
|----------|-------------|
| `FlattenList: { as: f1, index: f2 }` | Convert each element of a list to a datum with an additional index field. |
| `FlattenMatrix: { as: f1, index1: f2, index2: f3 }` | Convert each element of a matrix to a datum with row and column fields. |
| `OneHot: [ value1, value2, ... ]` | Convert an "enum" value into an array of 0 or 1. |

### Type Conversions

| Function | Description |
|----------|-------------|
| `As: type` | Convert value to target Java type, e.g. `As: Double` or `As: LocalDateTime`. |
| `Decode: decoder` | Convert value to target type using a custom decoder. |
| `DecodeInstant: format` | Convert value to an `Instant` using the given format string. |

**`Decode` decoder options:** `NULL`, `STRING`, `BOOLEAN`, `LONG`, `INTEGER`, `SHORT`, `BYTE`, `DOUBLE`, `FLOAT`,
`IP_ADDRESS`, `DOMAIN_NAME`, `HEX_STRING`, `LIST`, `DATE_TIME`, `DATE`, `TIME`, `EPOCH`

May also be a *type-parameter* object such as `InstantDecoder: "yyyy-MM-dd'T'HH:mm:ss"` or
`InstantEpochDecoder: "yyyy-MM-dd'T'HH:mm:ss"`.

## Value Transformations

Value transformations are considered the same as value computations.
