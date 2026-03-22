# JvmFinder

`jvmfinder` is a Gradle toolchains resolver implementation that uses, where possible, vendor-specific APIs to provision
toolchains. It can be used in place of or in addition to the foojay resolver.

## Usage

To get started, add the following to your `settings.gradle`:

```gradle
plugins {
    id "dev.lukebemish.jvmfinder-convention" version "<...>"
}
```

If you want to add te resolver without automatically configuring toolchain repositories, you can use the non-convention
plugin:

```gradle
plugins {
    id "dev.lukebemish.jvmfinder-convention" version "<...>"
}
```

## Supported JVM Distributions

Currently, the following JVM distributions are supported:
- Amazon Corretto
- Adoptium (Eclipse Temurin)
- IBM Semeru (through Adoptium Marketplace)
- Microsoft Build of OpenJDK (through Adoptium Marketplace)
- Azul Zulu
- Oracle GraalVM (somewhat experimental support)
