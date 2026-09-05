# Quartz AOT Processor

A multi-module Spring Boot utility designed to provide seamless **Ahead-of-Time (AOT) compilation** and **GraalVM Native Image** support for applications using the **Quartz Scheduler**.

Traditionally, Quartz relies heavily on dynamic reflection and runtime configuration, which breaks GraalVM native image compilation. This project introduces custom AOT processing to pre-register Quartz jobs, triggers, and connection providers during build time.

## Project Structure

- `quartz-aot-platform-core`: Core logic for analyzing Quartz configurations and generating runtime hints for reflection and proxying.
- `quartz-aot-platform-spring-boot-starter`: Auto-configuration module that integrates the AOT processor directly into any Spring Boot application.

## Features

- **Automated Reflection Hints:** Automatically detects and registers your custom Quartz `Job` classes for reflection.
- **GraalVM Native Compatibility:** Eliminates the need for manual `reflect-config.json` writing for Quartz components.
- **Spring Boot 3+ Ready:** Leverages the native Spring Boot AOT engine (`BeanFactoryInitializationAotProcessor`).

## Installation

Add the dependency to your Spring Boot project (Maven example):

```xml
<dependency>
    <groupId>io.github.bl00mfilter</groupId>
    <artifactId>quartz-aot-platform-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## How It Works (The Entry Point)

The starter hooks into Spring's context lifecycle via `QuartzAotAutoConfiguration`. During the AOT build phase, it triggers a specialized processor that scans the application context for Quartz scheduler beans, extracts metadata, and generates the necessary hints for the native compiler.
