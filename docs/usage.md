# Using Parsnip for Configurable Data Transformation

When using Parsnip, generally users will be configuring `Datum` operations.
For instance:

- When filtering or querying data, *Parsnip* allows you to configure a filter on arbitrary structured data, similar to a SQL query.
- When processing data, making one or more data, value, or filter operations, *Parsnip* can chain together multiple operations.

This section describes the starting point for these common use cases.

!!! note
    When representing Parsnip in YAML, it is common to represent types as an outer field and the type's parameters
    in the inner field, e.g. `Type: { key: value, key2: value2 }`.
    For types that do not require parameters, the syntax is `Type: { }`.
    *Parsnip* allows this syntax to be abbreviated in some cases when the type is clear from context.
    We call this the **type-parameter object convention**.

## Filtering Data

When filtering data, use the `DatumFilter` type within Parsnip, which provides an object that maps a *key-value map*
into a *boolean*. The following types of filters can be combined to create a `DatumFilter`:

- `DatumFieldFilter` filters datums based on values in specific fields using `ValueFilter`s, with the form
  `{ field1: filter1, field2: filter2, ... }`.

    - If the value is a scalar value (string, number, or boolean), the filter looks for a matching value.
      By default, the matching operation ignores types, i.e. the string `"10"` is considered the same as the integer
      `10` and the floating-point value `10.0`.
    - If the value is a list, the filter looks for a value matching any of the items in the list.
    - If the value is an object, the filter expects a *type-parameter object* representing a `ValueFilter`.
    - See [Datum Filters](reference/datum.md#datum-filters) and [Value Filters](reference/value.md#value-filters) for more details.

- `Not`, `And`, and `Or` logically combine 1 or more other filters into a single filter.
- `All` and `None` are provided for completeness.

Here is an example YAML configuration that uses both types of filters:

```yaml
Or:
  - DatumFieldFilter:
      /messageBody/sensorId: 2
      /messageBody/vehicleId: [1, 2, 3]
      /messageBody/state:
        Gte: 40.0
  - DatumFieldFilter:
      /messageBody/sensorId: 3
```

When using just a `DatumFieldFilter`, the object type can be omitted, so the above is equivalent to:

```yaml
Or:
  - /messageBody/sensorId: 2
    /messageBody/vehicleId: [1, 2, 3]
    /messageBody/state:
      Gte: 40.0
  - /messageBody/sensorId: 3
```

Here is the code for using a YAML-configured filter in Java:

```java
// load the filter from a YAML file or other source
File yamlSource = new File("filter.yaml");
ObjectMapper mapper = new YAMLMapper().registerModule(parsnipModule());
DatumFilter filter = mapper.readValue(yamlSource, DatumFilter.class);

// use it to filter data
Map<String, Object> datum = new LinkedHashMap<>();
boolean test = filter.invoke(datum);
```

## Notes on JSON and YAML

Examples using both *JSON* and *YAML* formats are provided throughout this documentation, although we prefer *YAML*
where possible. See [yaml.org](https://yaml.org/) for an introduction to YAML and
[json.org](https://www.json.org/) for an introduction to JSON.

The two representations are equivalent and easily interchanged, as in the following examples:

```yaml
Condition:
  - when:
      x:
        Gte: 5
    value:
      Field: a
  - when:
      x:
        Lt: 5
    value:
      Field: b
```

```json
{ "Condition": [
  {
    "when": { "x": { "Gte": 5 } },
    "value": { "Field": "a" }
  }, {
    "when": { "x": { "Lt": 5 } },
    "value": { "Field": "b" }
  }
] }
```

Since *JSON* is valid *YAML*, sometimes a hybrid approach is used to balance readability and space:

```yaml
Condition:
  - when: { x: { Gte: 5 }, value: { Field: a } }
  - when: { x: { Lt: 5 }, value: { Field: b } }
```

## Processing Data Sets

### Data Conversions

The `Create` type makes it easy to convert an input datum object to an output datum object by specifying the fields
that should be populated in the output and their associated functions. In YAML, this type is represented as a
*field-value* computation map of the form `{ field1: compute1, field2: compute2, ... }`.

Fields may be simple strings, or for nested results, can be JSON Pointer notation.

Here is an example YAML configuration of `Create`:

```yaml
text:
  Template: "{/a} {/b} and stuff"
  As: "String"
sensor: "my sensor"
state:
  Field: "xx"
  Lookup:
    x: 1
    y: "two"
source:
  Field: "source"
  IpToInt: {}
conditional result:
  Condition:
  - when: { x: { Gte: 5 } }
    value: { Field: a }
  - when: { x: { Lt: 5 } }
    value: { Field: b }
```

Here is an example in Java:

```java
String yaml = "description:\n  Template: \"{/a} and {/b}\"";
ObjectMapper mapper = new YAMLMapper().registerModule(parsnipModule());
Create create = mapper.readValue(yaml, Create.class);

Map<String, Object> input = ImmutableMap.of("a", "up", "b", "down");
Map<String, Object> output = create.invoke(input);
// output = { "description": "up and down" }
```

If all that is needed is to add fields on top of the existing data structure, use `Augment` instead.

### Data Extract-Transform-Load Pipeline

`Etl` is a special type in *Parsnip* that allows a complete **Extract-Transform-Load (ETL)** pipeline to be
configured in a single YAML file. It is configured as in the following example:

```yaml
extract:
  # DatumFieldFilter object definition with field keys and ValueFilter values
transform:
  - # DatumCompute object
  - # DatumCompute object
  - # ...
load:
  # Create object
```

When processing data, this converts an input datum to either null or an output datum.
If the *extract* condition does not match the data, the result is null.
Otherwise, a series of transformation steps are applied as `DatumCompute` objects, each of which converts the input
datum to another datum; if omitted, data is passed through unchanged.
The last step is the *load* operation, which constructs the resulting output object; if omitted, data is passed through unchanged.

Here are two complete example YAMLs:

```yaml
- extract: { sensorId: 2, "@type": ClockMessage }
  transform:
    - FlattenFields: [ timeUtc ]

- load:
    text:
      Template: "{/messageTimestamp} {/componentId} {/messageBody/sensor} {/messageBody/state}"
```

This can be used in Java as follows:

```java
// load the ETL from a YAML file or other source
File yamlSource = new File("pipeline.yaml");
ObjectMapper mapper = new YAMLMapper().registerModule(parsnipModule());
Etl pipeline = mapper.readValue(yamlSource, Etl.class);

// use it to process data
Map<String, Object> datum = new LinkedHashMap<>();
Map<String, Object> output = pipeline.invoke(datum);
```
