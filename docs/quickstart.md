# Quickstart

This guide walks you through the most common Parsnip use cases in a few steps.

## 1. Add the Dependency

```xml
<dependency>
    <groupId>com.googlecode.blaisemath</groupId>
    <artifactId>parsnip</artifactId>
    <version>2.0.0</version>
</dependency>
```

## 2. Register the Parsnip Jackson Module

Parsnip uses [Jackson](https://github.com/FasterXML/jackson) for JSON/YAML serialization. Register the module once
when setting up your `ObjectMapper`:

```java
ObjectMapper mapper = new YAMLMapper().registerModule(parsnipModule());
```

## 3. Load a Filter from YAML

```java
String yaml = "/messageBody/sensorId: 2";
DatumFilter filter = mapper.readValue(yaml, DatumFilter.class);

Map<String, Object> datum = Map.of("messageBody", Map.of("sensorId", 2));
boolean matches = filter.invoke(datum); // true
```

## 4. Load a Transformation from YAML

```java
String yaml = "description:\n  Template: \"{/a} and {/b}\"";
Create create = mapper.readValue(yaml, Create.class);

Map<String, Object> input = Map.of("a", "up", "b", "down");
Map<String, Object> output = create.invoke(input);
// output = { "description": "up and down" }
```

## 5. Run an ETL Pipeline

```java
String yaml =
    "- extract: { sensorId: 2 }\n" +
    "  transform:\n" +
    "    - FlattenFields: [ timeUtc ]\n";

Etl pipeline = mapper.readValue(yaml, Etl.class);

Map<String, Object> datum = ...; // your input
Map<String, Object> result = pipeline.invoke(datum);
```

## Next Steps

- [Using Parsnip](usage.md) for a full guide on filtering and transforming data.
- [Value Operations](reference/value.md), [Datum Transformations](reference/datum.md),
  [Set Operations](reference/set.md), and [DataSet Transformations](reference/dataset.md)
  for the complete operation reference.
- [API Reference](api/index.html) for full Kotlin/Java API documentation.
