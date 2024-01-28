# Case Class Initializer

## Overview

Case Class Initializer is a Scala library designed to simplify the initialization of case classes, especially those with nested structures and various types. It utilizes Scala reflection to dynamically generate initialization code with appropriate default values.

## Features

- **Dynamic Initialization**: Automatically generates initialization code for any Scala case class.
- **Support for Nested Structures**: Seamlessly handles nested case classes.
- **Customizable Default Values**: Allows customization of default values for different types.
- **Case Class Properties Listing**: Provides functionality to list all properties of a case class, including nested structures, in dot syntax.

## Prerequisites

- Scala 2.12 or 2.13
- Java 1.8 or higher

## Installation

Include this library in your Scala project by adding the following dependency: TODO

## Usage

### Basic Usage

To use the library, simply import the `CaseClassInitializer` object and call the `generateInitializationCode` method with your case class. For example:

```scala
import org.julianfrech.samples.CaseClassInitializer._

case class SampleClass(a: Int, b: String, c: Option[Boolean])
val initializationCode = generateInitializationCode[SampleClass]
println(initializationCode)
```

### Nested Case Classes
```scala
import org.julianfrech.samples.CaseClassInitializer._
import java.sql.Timestamp
import org.julianfrech.samples.CaseClassInitializer.generateInitializationCode

case class SampleNew(someId: Option[String], someOtherId: Option[String], timeStampCol: Option[Timestamp])
case class StructedCaseClass(identificator: Option[String] = None, content: Option[SampleNew] = None, contentAlternative: Option[SampleSmall] = None)
case class SampleSmall(someId: Option[String] = None, details: Option[SampleNumbers] = None)
case class SampleNumbers(someOtherId: Option[Int] = None, business: Option[String] = None)

print(generateInitializationCode[StructedCaseClass])
// Will return below
StructedCaseClass(
  identificator = Some(""),
  content = Some(SampleNew(
    someId = Some(""),
    someOtherId = Some(""),
    timeStampCol = Some(2024-01-24 10:17:43.139)
  )),
  contentAlternative = Some(SampleSmall(
    someId = Some(""),
    details = Some(SampleNumbers(
      someOtherId = Some(0),
      business = Some("")
    ))
  ))
)
```

### Listing Case Class Properties
To list the properties of a case class, use the listCaseClassProperties method:

```scala
import org.julianfrech.samples.CaseClassInitializer._

case class ExampleClass(a: Int, b: Option[String], c: NestedClass)
case class NestedClass(d: Double, e: String)

val properties = listCaseClassProperties[ExampleClass]
properties.foreach(println)
// Output
ExampleClass.a: Int = 0
ExampleClass.b: Option[String] = Some("")
ExampleClass.c.d: Double = 0.0
ExampleClass.c.e: String = ""
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
