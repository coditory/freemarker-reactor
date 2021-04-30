# Freemarker Reactor

[![Build Status](https://github.com/coditory/freemarker-reactor/workflows/Build/badge.svg)](https://github.com/coditory/freemarker-reactor/actions?query=workflow%3ABuild)
[![Coverage Status](https://coveralls.io/repos/github/coditory/freemarker-reactor/badge.svg)](https://coveralls.io/github/coditory/freemarker-reactor)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.coditory.freemarker/freemarker-reactor/badge.svg)](https://mvnrepository.com/artifact/com.coditory.freemarker/freemarker-reactor)

> [Freemarker](https://freemarker.apache.org/) templates for reactive applications using project Reactor

Most frameworks (Spring and Ktor) accept blocking templating mechanisms because they are heavily cached. This library
provides truly non-blocking api. For details read [how it works](#how-it-works).

## Installation

Add to your `build.gradle`:

```gradle
dependencies {
    compile 'com.coditory.freemarker:freemarker-reactor:0.1.0'
}
```

## Usage

```java
ReactiveFreeMarkerTemplateEngine engine=ReactiveFreeMarkerTemplateEngine.builder()
    .setTemplateLoader(new ReactiveFreeMarkerClasspathLoader("templates"))
    .build();

ReactiveFreeMarkerTemplate template=engine
    .createTemplate("template")
    .block();

String result=template.process(Map.of("a",true))
    .block();

System.out.println("Result:\n"+result);
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

- Template is loaded in a non-blocking manner
- Loaded template is parsed using Freemarker library (with blocking api)
- Template is resolved:
    - All includes and imports are registered as template dependencies (no IO operations)
    - All dependencies are resolved using non-blocking mechanism
    - Template is resolved again until all dependencies are loaded