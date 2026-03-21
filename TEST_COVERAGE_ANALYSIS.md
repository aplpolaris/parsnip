# Test Coverage Analysis

## Summary

The parsnip codebase has **48 test files** covering **~86 source files** across two modules (`parsnip` and `parsnip-types`). While coverage is reasonable in many areas, there are significant gaps in filtering, value computation, and utility code.

---

## Priority 1: Critical Gaps

These areas handle core computation logic and have no direct tests.

### `datum/compute/CalculateBoolean.kt`
Boolean expression evaluator built on top of a parser grammar. Converts JSON pointer fields to variables, evaluates conditions, and silently returns `null` on parse or evaluation errors. This silent-failure behavior makes untested edge cases especially dangerous.

**Suggested tests:**
- Valid AND/OR/NOT expressions
- Missing or null field references
- Malformed expression syntax (should return `null` gracefully)
- Type coercions (numeric to boolean)

### `datum/compute/Operate.kt`
An enum of 13 numeric operations (ADD, SUBTRACT, MULTIPLY, DIVIDE, GT, LTE, MIN, MAX, AVG, STD_DEV, etc.) used throughout datum-level compute logic. Division by zero, NaN, and null inputs are handled by a `tryInvoke` wrapper that swallows errors silently.

**Suggested tests:**
- Each operation with Int, Long, and Double inputs
- Division by zero and overflow behavior
- STD_DEV with 0, 1, and N values
- Min/Max with date-type inputs (`returnsDates = true`)
- Null/empty input handling per operation

---

## Priority 2: Value Filters (No Tests at All)

The entire `value/filter/` package is untested. These filters are used as predicates throughout dataset and ETL pipeline operations.

### `value/filter/ComparingValueFilter.kt`
Abstract filter with 10+ concrete implementations: `Equal`, `NotEqual`, `Gt`, `Gte`, `Lt`, `Lte`, `IsNull`, `IsNotNull`, `IsEmpty`, `IsNotEmpty`, `Range`. Null handling and comparison failures are silently caught and return `false`.

**Suggested tests:**
- Each filter type with numeric, string, date, and null values
- `Equal`/`NotEqual` null-safe semantics
- `Range` min/max boundary conditions (inclusive/exclusive)
- Incomparable type handling

### `value/filter/IpFilter.kt`
IP and CIDR matching filters (`IsIP`, `IsCidr`, `IpContainedIn`, `CidrContains`). These are used for network-based access control and classification.

**Suggested tests:**
- Valid and invalid IPv4 formats
- Valid and invalid CIDR notation
- IP-in-CIDR matching (boundary addresses)
- Non-string input coercion

### `value/filter/StringFilter.kt`
String matching filters: `Contains`, `StartsWith`, `EndsWith`, `Matches`, `ContainsMatch`.

**Suggested tests:**
- Each filter type with matching and non-matching strings
- Null input handling
- Regex compilation edge cases (invalid patterns, empty patterns)

### `value/filter/LogicalFilters.kt` and `datum/filter/LogicalFilters.kt`
Both packages have `All`, `None`, `And`, `Or`, `Not` filter classes with infix operators and flattening/unwrapping optimizations (e.g., `Not(Not(x))` → `x`, nested `And` flattening).

**Suggested tests:**
- `All` always returns `true`, `None` always returns `false`
- Nested `And`/`Or` combinations
- Double-negation unwrapping
- Nested `And`/`Or` flattening (concise list logic)
- Infix operators (`and`, `or`, `not`, `unaryMinus`)

### `value/filter/OneOf.kt`
Membership filter backed by `Equal`. Untested despite being commonly used.

**Suggested tests:**
- Membership with matching and non-matching values
- Empty list behavior
- Null values in the set

---

## Priority 3: Datum Transforms

### `datum/transform/Mapping.kt`
Applies field updates to a datum when a filter condition matches. Uses `nestedPutAll` for deep key insertion. No tests exist.

**Suggested tests:**
- Single filter match → mapping applied
- No matching filter → datum unchanged
- Multiple filters, only one matching
- Nested field path updates

### `dataset/transform/Chain.kt`
Contains `Limit` (take first N rows) and `Chain` (sequential dataset transforms). Early termination propagates `null` through the chain.

**Suggested tests:**
- `Limit` with N = 0, N > dataset size, N < dataset size
- `Chain` applying multiple transforms sequentially
- `Chain` with a transform that returns `null` (should stop the chain)
- Empty chain behavior

---

## Priority 4: Value Computations

### `value/compute/ValueFilterCompute.kt`
Wraps a `ValueFilter` as a `ValueCompute` (returns `true`/`false`). A simple but important bridge between the filter and compute hierarchies that is completely untested.

### `value/compute/TargetMultipleFields.kt`
A serialization marker class whose `invoke()` throws `IllegalStateException` by design. The exception path should be explicitly tested.

### `dataset/compute/Conversions.kt`
Extracts all values for a field across a dataset. Missing field handling silently returns `null`.

**Suggested tests:**
- Field present in all rows
- Field missing in some rows
- Empty dataset
- Nested field path resolution

---

## Priority 5: parsnip-types Utilities

These utility classes underpin many features but lack dedicated tests.

### `types/DateTimeFormats.kt`
Heuristic-based date format detection with a ranking algorithm that penalizes far-future and distant-past dates. Complex logic with multiple failure modes.

**Suggested tests:**
- Detection of common formats (ISO 8601, epoch millis, locale-specific)
- Ranking: prefer plausible dates over implausible ones
- Ambiguous formats (e.g., `01/02/03`)
- Invalid string handling

### `utilkt/core/KCollections.kt`
Extension functions: `toMap` (with transform), `mapCatching` (preserves exceptions).

**Suggested tests:**
- `mapCatching` with a mix of successes and failures
- `toMap` with duplicate keys
- Empty collection behavior

### `util/classifier/Classifier.kt`
Generic classifier interface with `bestGuess()` returning the highest-scoring category.

**Suggested tests:**
- `bestGuess()` with a clear winner
- Tied scores
- Empty classifier (should return `null` or throw — edge case worth verifying)

---

## Areas with Existing Tests Worth Expanding

| File | Existing Test | Gap |
|------|--------------|-----|
| `datum/compute/Calculate.kt` | `CalculateTest` | Boolean variant (`CalculateBoolean`) not covered |
| `datum/compute/Condition.kt` | `ConditionTest` | Only basic cases; `else` branches under-tested |
| `datum/filter/DatumFieldFilter.kt` | `DatumFieldFilterTest` | No logical combination tests |
| `decode/StandardDecoders.kt` | `StandardDecodersTest` | Edge cases for partial/invalid input |
| `io/ParsnipMapper.kt` | `ParsnipMapperTest` | Round-trip serialization of filters untested |

---

## Recommended Test Files to Create (in priority order)

1. `OperateTest.kt` — covers all 13 enum operations
2. `CalculateBooleanTest.kt` — covers boolean expression evaluation
3. `ComparingValueFilterTest.kt` — covers all comparison filters
4. `IpFilterTest.kt` — covers IP/CIDR matching
5. `StringFilterTest.kt` — covers string match filters
6. `ValueLogicalFiltersTest.kt` and `DatumLogicalFiltersTest.kt` — cover logical combinators
7. `MappingTest.kt` — covers conditional datum transforms
8. `DatasetChainTest.kt` — covers `Limit` and `Chain`
9. `DateTimeFormatsTest.kt` — covers format detection heuristics
10. `KCollectionsTest.kt` — covers collection utilities
