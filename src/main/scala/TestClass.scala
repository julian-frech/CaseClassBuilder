package org.julianfrech.samples

import java.sql.Timestamp

case class NestedClass(x: Int)

case class OuterClass(nested: NestedClass, flag: Boolean)

case class TestClass(a: Int, b: String, c: Option[Boolean])

case class SampleNew(someId: Option[String], someOtherId: Option[String], timeStampCol: Option[Timestamp])
case class StructedCaseClass(identificator: Option[String] = None, content: Option[SampleNew] = None, contentAlternative: Option[SampleSmall] = None)
case class SampleSmall(someId: Option[String] = None, details: Option[SampleNumbers] = None)
case class SampleNumbers(someOtherId: Option[Int] = None, business: Option[String] = None)