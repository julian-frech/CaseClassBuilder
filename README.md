# Case Class Initializer

## Overview

Case Class Initializer is a Scala library designed to simplify the initialization of case classes, especially those with nested structures and various types. It utilizes Scala reflection to dynamically generate initialization code with appropriate default values.

## Features

- **Dynamic Initialization**: Automatically generates initialization code for any Scala case class.
- **Support for Nested Structures**: Seamlessly handles nested case classes.
- **Customizable Default Values**: Allows customization of default values for different types.

## Prerequisites

- Scala 2.12 or 2.13
- Java 1.8 or higher

## Installation

Include this library in your Scala project by adding the following dependency:

## Usage

### Basic Usage

To use the library, simply import the `CaseClassInitializer` object and call the `generateInitializationCode` method with your case class. For example:

```scala
import org.julianfrech.samples.CaseClassInitializer._

case class SampleClass(a: Int, b: String, c: Option[Boolean])
val initializationCode = generateInitializationCode[SampleClass]
println(initializationCode)
```

## Customizing Default Values

You can easily customize default values for specific types by creating implicit instances of `DefaultValue[T]`. Here's how you can set a custom default value for `String`:

### Overwriting Default Values
```scala
implicit val customStringDefault: DefaultValue[String] = DefaultValue.createDefaultValue("custom default string")
```
### Adding Default Values
```scala
case class MyCustomClass(x: Int, y: String)
implicit val myCustomClassDefault: DefaultValue[MyCustomClass] = DefaultValue.createDefaultValue(MyCustomClass(0, ""))
```
