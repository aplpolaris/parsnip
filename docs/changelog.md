# Changelog

All notable changes to Parsnip are documented here.

## [Unreleased]

## [2.0.0]

- Kotlin rewrite of core library.
- Updated Jackson dependency to 2.x.
- Added `parsnip-types` module for general type deserialization utilities.
- Added `Etl` type for configurable extract-transform-load pipelines.
- Added `CalculateBoolean` for boolean template expressions.
- Added `Change` transformation for monitoring field changes.
- Added `Mapping` transformation for conditional field augmentation.
- Added `Fold` and `Flatten` multi-datum transformations.

## [1.0.0]

- Initial release.
- Core data types: value, set, datum, dataset.
- Filters, computations, and transformations for each data type.
- JSON/YAML serialization support via Jackson.
