package org.julianfrech.samples

import CaseClassInitializer.{generateInitializationCode, listCaseClassProperties}

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.sql.Timestamp
import scala.reflect.ClassTag

class CaseClassInitializerSpec extends AnyFlatSpec with Matchers {


  "DefaultValue" should "provide correct default values for standard types" in {
    DefaultValue[Int].value should be(0)
    DefaultValue[String].value should be("\"\"")
    DefaultValue[Boolean].value should be(false)
    //TODO: Test fruther default types are set
  }

  "generateInitializationCode" should "generate correct initialization code for case classes" in {

    val code = generateInitializationCode[TestClass]
    println(code)
    code should include("TestClass(")
    code should include("a = 0")
    code should include("b = \"\"")
    code should include("c = Some(false)")
  }

  it should "handle nested case classes correctly" in {


    val code = generateInitializationCode[OuterClass]
    println(code)
    code should include("OuterClass(")
    code should include("nested = Some(NestedClass(")
    code should include("x = 0")
    code should include("flag = false")

  }

  "DefaultValue" should "provide an empty array for array types" in {
    implicit def arrayDefault[T: ClassTag]: DefaultValue[Array[T]] =
      new DefaultValue[Array[T]] {
        val value = Array.empty[T]
      }

    val defaultIntArray: Array[Int] = DefaultValue[Array[Int]].value
    val defaultStringArray: Array[String] = DefaultValue[Array[String]].value

    defaultIntArray shouldBe Array.empty[Int]
    defaultStringArray shouldBe Array.empty[String]

    defaultIntArray should have length 0
    defaultStringArray should have length 0
  }

}

class CaseClassPropertiesSpec extends AnyFlatSpec with Matchers {

  "listCaseClassProperties" should "list properties of OuterClass" in {
    val properties = listCaseClassProperties[OuterClass]
    properties should contain("OuterClass.nested.x: Int = 0")
    properties should contain("OuterClass.flag: Boolean = false")
  }


  it should "correctly list properties for a simple case class" in {
    val properties = listCaseClassProperties[TestClass]
    properties should contain("TestClass.a: Int = 0")
    properties should contain("TestClass.b: String = \"\"")
    properties should contain("TestClass.c: Option[Boolean] = Some(false)")
  }

  it should "correctly list properties for a case class with a Seq of NestedClass" in {
    val properties = listCaseClassProperties[ClassWithSeqOfNested]
    properties should contain("ClassWithSeqOfNested.nestedSeq: Seq[NestedClass] = Seq.empty[NestedClass]")
  }

  it should "correctly list properties for a case class with an Option of Seq of NestedClass" in {
    val properties = listCaseClassProperties[ClassWithOptionSeqOfNested]
    properties should contain("ClassWithOptionSeqOfNested.optionalNestedSeq: Option[Seq[NestedClass]] = Some(Seq.empty[NestedClass])")
  }


}