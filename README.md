# Freemarker Reactor

[![Build Status](https://github.com/coditory/freemarker-reactor/workflows/Build/badge.svg)](https://github.com/coditory/freemarker-reactor/actions?query=workflow%3ABuild)
[![Coverage Status](https://coveralls.io/repos/github/coditory/freemarker-reactor/badge.svg)](https://coveralls.io/github/coditory/freemarker-reactor)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.coditory.freemarker/freemarker-reactor/badge.svg)](https://mvnrepository.com/artifact/com.coditory.freemarker/freemarker-reactor)

> [Freemarker](https://freemarker.apache.org/) templates for reactive applications using project Reactor

Most frameworks (Spring and Ktor) accept blocking templating mechanisms because they are heavily cached. This library
provides truly non-blocking api. For details read [how it works](#how-it-works).

**Advantages:**
- Designed to work with project Reactor
- Uses non-blocking IO
- Resolved Mono/Flux parameters
- Provides enhanced template resolution mechanism, that supports: relative paths, scoped templates and directory index file
- Provides template separation mechanism

## Installation

Add to your `build.gradle`:

```gradle
dependencies {
    compile 'com.coditory.freemarker:freemarker-reactor:0.1.1'
}
```

## Usage

```java
ReactiveFreeMarkerTemplateEngine engine = ReactiveFreeMarkerTemplateEngine.create();

ReactiveFreeMarkerTemplate template = engine
    .createTemplate("template")
    .block();

Map<String, Object> params = Map.of("a",true, b, Mono.just("B"));

String result=template.process(params)
    .block();

System.out.println("Result:\n"+result);
```

**Default behavior:**
- templates are resolved from classpath, under `templates` directory
- templates (and all includes/imports) are cached
- templates extension is `.ftl`

Sometimes it is desired to skip caches and read templates directly from 
project files:

```java
TemplateEngine engine = TemplateEngine.builder()
    .setTemplateLoader(new FileTemplateLoader("src/main/resources/templates"))
    .removeCache()
    .build();
```

## Directives

### Import

Instead of using original and synchronous `<#import ...>` use `<@import ...>`.

**Example:**

- `<@import "./a">` - imports template "./a" as "a"
- `<@import name="./a" ns="b">` - imports template "./a" as "b"

**Usage:**

```
<@import "./a">

Greetings:
<#a.greetings user=${user}/>
```

**Parameters:**

- `name` - points to the template (see [Template Resolution](#template-resolution))
- `ns` - (default: derived from template file name) namespace for imported template macros

### Include

Instead of using original and synchronous `<#include ...>` use `<@include ...>`.

**Example:**

- `<@include "./a">` - imports template "./a"
- `<@include name="./a" parse=false required=false>` - imports optional template "./a" without interpreting

**Parameters:**

- `name` - points to the template (see [Template Resolution](#template-resolution))
- `parse` - (default: true) if included template should be interpreted
- `required` - (default: true) if included template is required

## Template Resolution

This library modifies FreeMarker template resolution mechanism. New mechanism handles [relative paths](#relative-paths)
, [scoped templates](#scoped-templates) and [directory index files](#directory-index-file).

### Relative Paths

- `<@include "lorem">` - includes `lorem.ftl` template from the base path
- `<@include "./lorem">` - includes `lorem.ftl` template relative to the template where the include was used
- `<@include "../lorem">` - includes `lorem.ftl` template relative to the template where the include was used Relative
  paths work with both `<@include ...>` and `<@import ...>` directives.

### Scoped templates

Scoped templates provide template encapsulation that work similar to java package scope. Whenever there is a template
that should not be included/imported from other directory, prefix its name with `_`.

Example:

- `lorem/_header.ftl` - can be imported only from `lorem` directory
- `lorem/footer.ftl` - can be imported be every other template

### Directory index file

It's often that a huge template file is split into smaller files in a single directory. Example:

```
// Before
./lorem.ftl

// After
./lorem/_index.ftl
./lorem/_header.ftl
./lorem/_main.ftl
./lorem/_footer.ftl
```

For such scenarios when there is no template for `<@include "lorem">` an alternative path is
checked `<@include "lorem/_index">`.

# How it works

How non-blocking templating works:

- Template Mono/Flux parameters are resolved
- Template is loaded in a non-blocking manner
- Loaded template is parsed using Freemarker library
- Template is resolved with a mocked template loader:
    - All includes and imports are registered as template dependencies (no IO operation is made)
    - All dependencies are resolved using non-blocking IO
    - Template is resolved again until all dependencies are resolved

# Warning
Until version `1.0.0` this library is under heavy development.
The API may change without backward compatibility.