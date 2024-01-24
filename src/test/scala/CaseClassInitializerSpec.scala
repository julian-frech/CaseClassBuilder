package org.julianfrech.samples

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.sql.Timestamp
import org.julianfrech.samples.CaseClassInitializer.generateInitializationCode

class CaseClassInitializerSpec extends AnyFlatSpec with Matchers {

  "DefaultValue" should "provide correct default values for standard types" in {
    DefaultValue[Int].value should be(0)
    DefaultValue[String].value should be("\"\"")
    DefaultValue[Boolean].value should be(false)
    // Test other types as needed
  }

  it should "provide a current timestamp for Timestamp type" in {
    val currentTimestamp = new Timestamp(System.currentTimeMillis())
    DefaultValue[Timestamp].value.getTime should be <= currentTimestamp.getTime
  }

  "generateInitializationCode" should "generate correct initialization code for case classes" in {

    val code = generateInitializationCode[TestClass]
    code should include("TestClass(")
    code should include("a = 0")
    code should include("b = \"\"")
    code should include("c = Some(false)")
  }

  it should "handle nested case classes correctly" in {


    val code = generateInitializationCode[OuterClass]
    code should include("OuterClass(")
    code should include("nested = NestedClass(")
    code should include("x = 0")
    code should include("flag = false")
  }

}
