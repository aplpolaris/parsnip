###################################################
Using Parsnip for Configurable Data Transformation
###################################################

When using Parsnip, generally users will be configuring ``Datum`` operations.
For instance:

- When filtering or querying data, *Parsnip* allows you to configure a filter on arbitrary structured data, similar to a SQL query.
- When processing data, making one or more data, value, or filter operations, *Parsnip* can chain together multiple operations.

This section describes the starting point for these common use cases.

.. note:: When representing Parsnip in YAML, it is common to represent types as an outer field and the type's parameters in the inner field, e.g. ``Type: { key: value, key2: value2 }``.
          For types that do not require parameters, the syntax is ``Type: { }``.
          *Parsnip* allows this syntax to be abbreviated in some cases when the type is clear from context.
          We call this the **type-parameter object convention**.

Filtering Data
---------------------
When filtering data, use the ``DatumFilter`` type within Parsnip, which provides an object that maps a *key-value map* into a *boolean*.
The following types of filters can be combined to create a ``DatumFilter``:

- ``DatumFieldFilter`` filters datums based on values in specific fields using ``ValueFilter``s, with the form ``{ field1: filter1, field2: filter2, ... }``.

  - If the value is a scalar value (string, number, or boolean), the filter looks for a matching value.
    By default, the matching operation ignores types, i.e. the string `"10"` is considered the same as the integer `10` and the floating-point value `10.0`.
  - If the value is a list, the filter looks for a value matching any of the items in the list.
  - If the value is an object, the filter expects a *type-parameter object* representing a ``ValueFilter``.
  - See :ref:`datum-filters` for more on datum filters and :ref:`value-filters` for the specific types of value filters supported.

- ``Not``, ``And``, and ``Or`` logically combine 1 or more other filters into a single filter.
- ``All`` and ``None`` are provided for completeness.

Here is an example YAML configuration that uses both types of filters:

.. code-block:: yaml

  Or:
    - DatumFieldFilter:
        /messageBody/sensorId: 2
        /messageBody/vehicleId: [1, 2, 3]
        /messageBody/state:
          Gte: 40.0
    - DatumFieldFilter:
        /messageBody/sensorId: 3

When using just a ``DatumFieldFilter``, the object type can be omitted, so the above is equivalent to:

.. code-block:: yaml

  Or:
    - /messageBody/sensorId: 2
      /messageBody/vehicleId: [1, 2, 3]
      /messageBody/state:
        Gte: 40.0
    - /messageBody/sensorId: 3

Here is the code for using a YAML-configured mapper in Java code:

.. code-block:: java

  // here's where we load the filter from a YAML file or other source
  File yamlSource = new File();
  ObjectMapper mapper = new YAMLMapper().registerModule(parsnipModule());
  DatumFilter filter = mapper.readValue(yamlSource, DatumFilter.class);

  // now we can use it to filter data, assuming we get datum from somewhere interesting
  Map<String, ? extends Object> datum = new LinkedHashMap<>();
  boolean test = filter.invoke(datum);

Processing Data Sets
---------------------

Data Conversions
*********************
The ``Create`` type makes it easy to convert an input datum object to an output datum object by specifying the fields that should be populated in the output and their associated functions.
In YAML, this type is represented as a *field-value computation map of the form ``{ field1: compute1, field2: compute2, ... }``.
Fields may be simple strings, or for nested results, can be JSON Pointer notation.
The following type of "compute" values are supported:

- If the value is a scalar value (string, number, or boolean), the field will be populated with the given constant.
- If the value is an object with a single key-value pair, it should be a *type-parameter object* for ``DatumCompute``.
- If the value is an object with multiple key-value pairs, the first should be a ``DatumCompute`` object, and the rest should be ``ValueCompute`` objects (must have different types to ensure they have different keys).
- If the value is a list, the first should be a *type-parameter object* for ``DatumCompute`` and the remainder should be ``ValueCompute`` objects.
- See :ref:`datum-compute` and :ref:`value-compute` for the specific types of computations supported. In some cases ``ValueFilter`` objects can be used in place of ``ValueCompute`` objects to provide boolean values.

Here is an example YAML configuration of ``Create``:

.. code-block:: yaml

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

Here is an example in Java:

.. code-block:: java

  String yaml = "description:\\n  Template: {/a} and {/b}"
  ObjectMapper mapper = new YAMLMapper().registerModule(parsnipModule());
  Create create = mapper.readValue(yaml, Create.class);

  Map<String, ? extends Object> input = ImmutableMap.of("a", "up", "b", "down");
  Map<String, ? extends Object> output = create.invoke(input);

  assertEquals(ImmutableMap.of("description", "up and down"), output);

If all that is needed is to add fields on top of the existing data structure, use ``Augment`` instead:

.. code-block:: java

  String yaml = "description:\\n  Template: {/a} and {/b}"
  ObjectMapper mapper = new YAMLMapper().registerModule(parsnipModule());
  Create create = mapper.readValue(yaml, Create.class);

  Map<String, ? extends Object> input = ImmutableMap.of("a", "up", "b", "down");
  Map<String, ? extends Object> output = create.invoke(input);

  assertEquals(ImmutableMap.of("a", "up", "b", "down", "description", "up and down"), output);

Data Extract-Transform-Load Pipeline
************************************
``Etl`` is a special type in *Parsnip* that allows a complete **Extract-Transform-Load (ETL)** pipeline to be configured in a single YAML file.
It is configured as in the following example:

.. code-block:: yaml

  extract:
    // DatumFieldFilter object definition with field keys and ValueFilter values
  transform:
    - // DatumCompute object
    - // DatumCompute object
    - // ...
  load:
    // Create object

When processing data, this converts an input datum to either null or an output datum.
If the *extract* condition does not match the data, the result is null.
Otherwise, a series of transformation steps are applied as ``DatumCompute`` objects, each of which converts the input datum to another datum; if omitted, data is passed through unchanged.
The last step in the process is the *load* operation, which constructs the resulting output object; if omitted, data is passed through unchanged.

Here are two complete example YAMLs:

.. code-block:: yaml

  - extract: { sensorId: 2, "@type": ClockMessage }
    transform:
      - FlattenFields: [ timeUtc ]

  - load:
      text:
        Template: "{/messageTimestamp} {/componentId} {/messageBody/sensor} {/messageBody/state}"

This can be used in Java as follows:

.. code-block:: java

  // here's where we load the filter from a YAML file or other source
  File yamlSource = new File();
  ObjectMapper mapper = new YAMLMapper().registerModule(parsnipModule());
  Etl pipeline = mapper.readValue(yamlSource, Etl.class);

  // now we can use it to process data, assuming we get datum from somewhere interesting
  Map<String, ? extends Object> datum = new LinkedHashMap<>();
  Map<String, ? extends Object> output = pipeline.invoke(datum);