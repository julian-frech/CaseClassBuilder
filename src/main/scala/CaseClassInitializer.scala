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
    case t if t <:< typeOf[Product] && t.typeSymbol.isClass =>
      val classSymbol = t.typeSymbol.asClass
      val companionSymbol = classSymbol.companion.asModule
      val companionMirror = runtimeMirror(getClass.getClassLoader).reflectModule(companionSymbol)
      val companionInstance = companionMirror.instance
      val companionType = companionSymbol.typeSignature

      // Look for an "apply" method in the companion object which has default parameters for all arguments
      val applyMethod = companionType.decl(TermName("apply")).asMethod
      if (applyMethod.paramLists.head.forall(_.asTerm.isParamWithDefault)) {
        val defaultParams = applyMethod.paramLists.head.indices.map { i =>
          val methodName = TermName(s"apply$$default$$${i + 1}")
          companionType.decl(methodName).asMethod
        }
        val values = defaultParams.map { method =>
          val methodMirror = runtimeMirror(getClass.getClassLoader).reflect(companionInstance)
          methodMirror.reflectMethod(method)().toString
        }
        s"${companionSymbol.name}(${values.mkString(", ")})"
      } else "Nested case class default value generation not implemented"
    // ... More cases for collections, arrays, etc.
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
    val optionType = typeOf[Option[Any]] // Handle Option types using Option[Any]

    def typeToString(t: Type): String = t match {
      case TypeRef(_, sym, args) =>
        val argsStr = args.map(typeToString).mkString(", ")
        if (args.nonEmpty) s"${sym.name.decodedName}[$argsStr]" else sym.name.decodedName.toString
      case _ => t.typeSymbol.name.decodedName.toString
    }

    def listFields(t: Type, prefix: String = ""): List[String] = {
      t.decls.collect {
        case m: MethodSymbol if m.isCaseAccessor =>
          val fieldName = m.name.decodedName.toString
          val fieldType = m.returnType
          val fieldTypeName = typeToString(fieldType)

          fieldType match {
            case t if t <:< optionType =>
              // Handle Option type
              val innerType = t.typeArgs.head
              val innerTypeName = typeToString(innerType)
              List(s"$prefix$fieldName: Option[$innerTypeName] = Some(${defaultValueForType(innerType)})")
            case t if t <:< typeOf[Product] && t.typeSymbol.isClass =>
              // Nested case class, list its fields
              listFields(t, s"$prefix$fieldName.")
            case _ =>
              val defaultValue = defaultValueForType(fieldType)
              List(s"$prefix$fieldName: $fieldTypeName = $defaultValue")
          }
      }.flatten.toList
    }

    val prefix = tpe.typeSymbol.name.decodedName.toString + "."
    listFields(tpe, prefix)
  }


}
