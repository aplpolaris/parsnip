# DataSet Transformations

## DataSet Filters

No dataset filters (mapping sets to booleans) are provided yet.

## DataSet Computations

Dataset computations convert an input dataset to an output value (other than a dataset).

| Function | Description |
|----------|-------------|
| `ArgMin: field` | Return datum in dataset whose field is minimized. |
| `ArgMax: field` | Return datum in dataset whose field is maximized. |

## DataSet Transformations

### Basic Functions

| Function | Description |
|----------|-------------|
| `Limit: n` | Return the first *n* elements of data. |
| `Chain: [ t1, t2, t3 ]` | Perform a sequence of transformations on the dataset. |

### Sorting

Sorting operations rearrange the input data.

| Function | Description |
|----------|-------------|
| `SortBy: [ field1, field2, ... ]` | Sort data in ascending order by the given fields. |
| `SortByDescending: [ field1, field2, ... ]` | Sort data in descending order by the given fields. |

### Aggregation

The aggregate operation is a very powerful way to rearrange and summarize data.

**`Aggregate: { op: {}, field: x, asField: x, groupBy: [] }`**

Aggregate data in a given field, optionally grouping the data by one or more other fields.

- **`op`**: a *set computation* operation such as `Count`, `Min`, `Max`, etc. defining the aggregation function
  (see [Set Computations](set.md#set-compute) for all options)
- **`field`**: the field that the set computation will be applied to (may be omitted for the `Count` operation)
- **`asField`**: the field name to use for the aggregated value in the result
- **`groupBy`**: (optional) list of field names to group by; each distinct tuple here will provide a distinct datum
  in the result
