package org.julianfrech.samples

import java.sql.Timestamp
import scala.reflect.runtime.universe._

object CaseClassInitializer extends App {

  def generateInitializationCode[T: TypeTag]: String = {
    def defaultValueForType(t: Type): String = t match {
      case t if t =:= typeOf[String] => DefaultValue[String].value
      case t if t =:= typeOf[Int] => DefaultValue[Int].value.toString
      case t if t =:= typeOf[Double] => DefaultValue[Double].value.toString
      case t if t =:= typeOf[Float] => DefaultValue[Float].value.toString
      case t if t =:= typeOf[Long] => DefaultValue[Long].value.toString
      case t if t =:= typeOf[Boolean] => DefaultValue[Boolean].value.toString
      case t if t =:= typeOf[Char] => s"'${DefaultValue[Char].value}'"
      case t if t =:= typeOf[Byte] => DefaultValue[Byte].value.toString
      case t if t =:= typeOf[Short] => DefaultValue[Short].value.toString
      case t if t =:= typeOf[Timestamp] => DefaultValue[Timestamp].value.toString
      case _ => "???"
    }

    def initCaseClass(t: Type, indent: String = ""): String = {
      val params = t.decls.collectFirst {
        case m: MethodSymbol if m.isPrimaryConstructor => m
      }.get.paramLists.head

      val paramStrings = params.map { p =>
        val paramName = p.name.toString
        val paramType = p.typeSignature

        paramType match {
          case t if t <:< typeOf[Option[_]] =>
            val innerType = t.typeArgs.head
            if (innerType <:< typeOf[Product] && innerType.typeSymbol.isClass) {
              s"$paramName = Some(${initCaseClass(innerType, indent + "  ")})"
            } else {
              val defaultValue = defaultValueForType(innerType)
              s"$paramName = Some($defaultValue)"
            }
          case t if t <:< typeOf[Product] && t.typeSymbol.isClass =>
            s"$paramName = ${initCaseClass(t, indent + "  ")}"
          case _ =>
            val defaultValue = defaultValueForType(paramType)
            s"$paramName = $defaultValue"
        }
      }

      s"${t.typeSymbol.name}(\n$indent  ${paramStrings.mkString(s",\n$indent  ")}\n$indent)"
    }

    initCaseClass(typeOf[T])
  }


}
