# Installation

## Requirements

- Java 11 or higher
- Maven 3.6.3 or higher (for building from source)

## Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.googlecode.blaisemath</groupId>
    <artifactId>parsnip</artifactId>
    <version>2.0.0</version>
</dependency>
```

If you only need the type utilities:

```xml
<dependency>
    <groupId>com.googlecode.blaisemath</groupId>
    <artifactId>parsnip-types</artifactId>
    <version>2.0.0</version>
</dependency>
```

Check [Maven Central](https://search.maven.org/search?q=g:com.googlecode.blaisemath) for the latest available version.

## Building from Source

Clone the repository and build with Maven:

```bash
git clone https://github.com/aplpolaris/parsnip.git
cd parsnip
mvn install
```
