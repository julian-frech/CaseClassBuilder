package org.julianfrech.samples

import java.sql.Timestamp
import scala.reflect.runtime.universe._

object CaseClassInitializer {
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
      case t if t.typeSymbol == typeOf[Option[_]].typeSymbol =>
        val innerType = t.typeArgs.head
        val innerDefaultValue = defaultValueForType(innerType)
        s"Some($innerDefaultValue)"
      case _ => "???"
  }
  def generateInitializationCode[T: TypeTag]: String = {


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
  def listCaseClassProperties[T: TypeTag]: List[String] = {
    val tpe = typeOf[T]

    def typeToString(t: Type): String = t match {
      case TypeRef(_, sym, args) =>
        args match {
          case Nil => sym.name.decodedName.toString
          case _ => s"${sym.name}[${args.map(typeToString).mkString(", ")}]"
        }
      case _ => t.toString
    }

    val fields = tpe.decls.collect {
      case m: MethodSymbol if m.isCaseAccessor =>
        val fieldName = m.name.decodedName.toString
        val fieldType = m.returnType
        val fieldTypeName = typeToString(fieldType)
        val defaultValue = CaseClassInitializer.defaultValueForType(fieldType)
        s"${tpe.typeSymbol.name.decodedName}.$fieldName: $fieldTypeName = $defaultValue"
    }.toList

    fields
  }

}
