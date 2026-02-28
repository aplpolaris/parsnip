# Datum Transformations

Recall that *datums* are maps (or dictionaries for Python users) with string keys and arbitrary values.
This is equivalent to rows of data in a database, lines in a CSV file, etc.

## Datum Filters { #datum-filters }

### Filtering by Fields

A *compound field filter* called **DatumFieldFilter** filters datums based on expected values for individual fields.
It is configured as a key-value map where the keys are fields (or JSON pointers) and values may be values, lists of
values, or other filter objects.

**`DatumFieldFilter: { fieldPointer1: value/filter1, fieldPointer2: value/filter2, ... }`**

Filter data by requiring fields to match given values or filters. Keys are either fields or *JSON pointers* to
fields, and values are either scalar values (for exact matching), list values (for matching multiple values), or
filter objects (for general matches).

The values for each field may be:

- A **scalar value** — the filter looks for a match with the associated field. Type comparison is flexible,
  i.e. the string `"10"` is considered the same as the integer `10` and the floating-point value `10.0`.
- A **list value** — the filter looks for matches with any of the values in the list.
- An **object** — it is assumed to be a *value filter* configured with a single key indicating the filter type.

**Example:**

```yaml
DatumFieldFilter:
  /messageBody/sensorId: 2
  /messageBody/vehicleId: [1, 2, 3]
  /messageBody/state:
    Gte: 40.0
```

### Combination Tests

| Function | Description |
|----------|-------------|
| `All: {}` | Matches all values (always returns true). |
| `None: {}` | Matches no values (always returns false). |
| `Not: filter` | Negates the given filter, e.g. `Not: { DatumFieldFilter: { ... } }`. |
| `And: [ filter1, filter2, ... ]` | Returns true if all provided filters match. |
| `Or: [ filter1, filter2, ... ]` | Returns true if any of the provided filters match. |

## Datum Computations { #datum-compute }

Datum computations convert an input datum to an output value (other than a datum).

### Basic

| Function | Description |
|----------|-------------|
| `Constant: x` | Return a fixed value no matter the input. |
| `Field: "f"` | Return the value in the given field. |

### Conditionals

Conditional functions calculate values differently depending on different conditions:

**`Condition: [ { when: { field1: filter1, ... }, value: compute1 }, ... ]`**

Return a different computed value depending on which filter matches first.

- **`when`**: A key-value object where keys are *field* names and values are `ValueFilter` objects.
- **`value`**: A `DatumCompute` object used to compute the return value when this condition is met.

**Example:**

```yaml
- Condition:
    - when: { /messageBody/state: { Lt: 20.0 } }
      value: { Constant: low }
    - when: { /messageBody/state: { Range: [20.0, 39.99] } }
      value: { Constant: med }
    - when: { /messageBody/state: { Gt: 40.0 } }
      value: { Constant: high }
```

### Compound

**`Chain: { from: compute1, process: [ compute2, compute3, ... ] }`**

Apply a single datum computation, followed by multiple value computations, allowing a sequence of operations to
be performed before obtaining a final result.

- **`from`**: A `DatumCompute` object that is applied first.
- **`process`**: Zero or more subsequent computations of type `ValueCompute`.

**Example:**

```yaml
Chain:
  from:
    Field: a.capital
  process:
    - Add: 100.0
    - Divide: 2.0
```

### Numbers

| Function | Description |
|----------|-------------|
| `Calculate: "template expression"` | Compute a mathematical expression on datum fields. Supports JSON pointer notation in braces, e.g. `{a} + 2`. |
| `MathOp: { operator: XX, fields: [ f1, f2, ... ], ifInvalid: x }` | Apply the given numeric operation to values in the given fields. |
| `CalculateBoolean: "template expression"` | Compute a boolean expression on datum fields, e.g. `{a} or {b}`. |

**`MathOp` operator options:**
Numeric: `ADD`, `SUBTRACT`, `NEGATE`, `MULTIPLY`, `DIVIDE`, `MIN`, `MAX`, `AVERAGE`, `STD_DEV`
Boolean: `EQUAL`, `NOT_EQUAL`, `GT`, `GTE`, `LT`, `LTE`

### Strings

**`Template: "template string"`** — Construct a string from a combination of field values using a template syntax,
e.g. `Template: "Door {/doorNumber} {/trigger}"`.

Supported syntaxes:

- A JSON pointer to a field, e.g. `/body/door/id`
- A semicolon-separated list of JSON pointers, e.g. `/path/1;/path/2` — uses the first pointer it finds with content
- A JSON pointer *template* e.g. `http://{/server/name}/stuff/{/server/path}.png` — inserts values into the template

### Structure

**`ToArray: bool`** — Convert a datum to an array of values, throwing out the keys.

- **`flatten`**: if true, values are flattened (so `{a:1, b:[2, 3]}` converts to `[1, 2, 3]`); otherwise `{a:1, b:[2, 3]}` → `[1, [2, 3]]`

## Datum Transformations { #datum-transform }

Datum transformations convert an input datum to an output datum.

### Basic

| Function | Description |
|----------|-------------|
| `Identity: {}` | Return the input value without making any changes. |
| `LogDatum: { level: "X" }` | Log and return the input datum. Level is one of `SEVERE`, `WARNING`, `INFO`, `CONFIG`, `FINE`, `FINER`, `FINEST`. |

### Field Operations

| Function | Description |
|----------|-------------|
| `RemoveFields: [ f1, f2, ... ]` | Remove given fields from the datum and return the result. |
| `RetainFields: [ f1, f2, ... ]` | Retain given fields from the datum, removing everything else, and return the result. |
| `FlattenFields: [ f1, f2, ... ]` | Flatten nested fields inside the given list of fields, e.g. `"f": { "a": 3 }` flattens to `"f.a": 3`. |
| `Symmetry: { f1: f1s, f2: f2s, ... }` | Swap values in each provided pair of fields, e.g. `Symmetry: { ip1: ip2, port1: port2 }`. |

### Field Creation

| Function | Description |
|----------|-------------|
| `Create: { "f1": compute1, "f2": compute2, ... }` | Return datum whose values are computed using the provided compute functions. |
| `Augment: { "f1": compute1, "f2": compute2, ... }` | Add new values to the input datum, using the given computations, and return the result. |

### Mapping and Conditional Operations

**`Mapping: [ { when: { field1: filter1, ... }, put: datum }, ... ]`**

Adds content to input datum for each matching filter and returns the augmented result.

- **`when`**: key-value object where keys are *field* names and values are `ValueFilter` objects.
- **`put`**: key-value object with content to be added to the datum if the filter passes.

**`Change: { monitor: "pointerField", groupBy: "pointerField", whenChange: [ { from: x1, to: x2, put: datum }, ... ] }`**

Monitors a single field for changes, adding content to the input datum when certain kinds of changes are made.

- **`monitor`**: a single field or JSON pointer being tracked for changes
- **`groupBy`**: (optional) field or JSON pointer; maintains a separate "last value" for each value in this field
- **`whenChange`**: (optional) list of specific content to add to input datum when changes are detected
- **`whenChange.from`**: (optional) value changing from; if null or omitted, tracks arbitrary values
- **`whenChange.to`**: (optional) value changing to; if null or omitted, tracks arbitrary values
- **`whenChange.put`**: (optional) key-value object with content to be added to the datum for this change condition

**Example:**

```yaml
- Mapping:
    - when: { /messageBody/state: { Lt: 20.0 } }
      put: { state: low }
    - when: { /messageBody/state: { Range: [20.0, 39.99] } }
      put: { state: med }
    - when: { /messageBody/state: { Gt: 40.0 } }
      put: { state: high }
- Change:
    monitor: state
    groupBy: /messageBody/sensorId
```

## Multi-Datum Transformations

Multi-datum transformations convert a single input datum to multiple output datums (a dataset).

### Structure

**`Fold: { fields: [ f1, f2, ... ], as: [ n1, n2 ] }`**

Split a datum into multiple datums, one for each field given. The field name and value are encoded in new fields.
For instance `Fold: { fields: [ a, b ] }` converts `{a:1, b:2}` to `[{a:1, b:2, key:a, value:1}, {a:1, b:2, key:b, value:2}]`.

- **`fields`**: list of fields to pull out as separate key-value pairs
- **`as`**: (optional) list of two names to use for key and value fields; defaults to `[ key, value ]`

**`Flatten: { fields: [ f1, f2, ... ], as: [ n1, n2, ... ] }`**

Split a datum into several datums by extracting values from lists for the given fields.
For instance `Flatten: { fields: [a] }` converts `{a: [1,2]}` to `[{a:1}, {a:2}]`.

- **`fields`**: list of fields whose values should be flattened into multiple datums
- **`as`**: (optional) list of field names to use in the result; if omitted, uses the source field names
