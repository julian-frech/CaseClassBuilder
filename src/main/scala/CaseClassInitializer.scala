package org.julianfrech.samples

import java.sql.Timestamp
import scala.reflect.runtime.universe._

object CaseClassInitializer {
  def defaultValueForType(t: Type): String = t match {
    case t if t =:= typeOf[Long] => DefaultValue[Long].value.toString + "L" // Add 'L' for Long literals
    case t if t =:= typeOf[Float] => DefaultValue[Float].value.toString + "f" // Add 'f' for Float literals
    case t if t =:= typeOf[Double] => DefaultValue[Double].value.toString + "d" // Add 'd' for Double literals
    case t if t =:= typeOf[String] => DefaultValue[String].value
    case t if t =:= typeOf[Int] => DefaultValue[Int].value.toString
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

  def generateInitializationCode2[T: TypeTag]: String = {


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
    val collectionTypes = Seq(typeOf[Seq[Any]], typeOf[List[Any]], typeOf[Vector[Any]], typeOf[Set[Any]])

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
              val innerType = t.typeArgs.head
              innerType match {
                case it if collectionTypes.exists(ct => it <:< ct) =>
                  val elementType = it.typeArgs.head
                  val elementTypeName = typeToString(elementType)
                  val collectionTypeName = innerType.typeSymbol.name.decodedName.toString
                  if (elementType <:< typeOf[Product] && elementType.typeSymbol.isClass) {
                    // Option[Collection[Case Class]]
                    List(s"$prefix$fieldName: Option[$collectionTypeName[$elementTypeName]] = Some($collectionTypeName.empty[$elementTypeName])")
                  } else {
                    // Other Option[Collection] types
                    List(s"$prefix$fieldName: Option[$collectionTypeName[$elementTypeName]] = Some($collectionTypeName.empty)")
                  }
                case it if it <:< typeOf[Product] && it.typeSymbol.isClass =>
                  // Option[Nested Case Class]
                  listFields(it, s"$prefix$fieldName.")
                case _ =>
                  // Other Option types
                  val defaultValue = defaultValueForType(innerType)
                  List(s"$prefix$fieldName: Option[$innerType] = Some($defaultValue)")
              }

            case t if collectionTypes.exists(ct => t <:< ct) =>
              // Direct Collection Types
              val elementType = t.typeArgs.head
              val elementTypeName = typeToString(elementType)
              val collectionTypeName = fieldType.typeSymbol.name.decodedName.toString
              if (elementType <:< typeOf[Product] && elementType.typeSymbol.isClass) {
                // Collections of case classes
                List(s"$prefix$fieldName: $collectionTypeName[$elementTypeName] = $collectionTypeName.empty[$elementTypeName]")
              } else {
                // Collections of simple types
                List(s"$prefix$fieldName: $collectionTypeName[$elementTypeName] = $collectionTypeName.empty")
              }

            case _ =>
              // Simple type, not wrapped in Option or Collection
              val defaultValue = defaultValueForType(fieldType)
              List(s"$prefix$fieldName: $fieldTypeName = $defaultValue")
          }
      }.flatten.toList
    }

    val prefix = tpe.typeSymbol.name.decodedName.toString + "."
    listFields(tpe, prefix)
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
            if (innerType <:< typeOf[Seq[_]] && innerType.typeArgs.head <:< typeOf[Product]) {
              val seqInnerType = innerType.typeArgs.head
              s"$paramName = Some(Seq(${initCaseClass(seqInnerType, indent + "  ")}))"
            } else if (innerType <:< typeOf[Product] && innerType.typeSymbol.isClass) {
              s"$paramName = Some(${initCaseClass(innerType, indent + "  ")})"
            } else {
              s"$paramName = Some(${defaultValueForType(innerType)})"
            }
          case t if t <:< typeOf[Product] && t.typeSymbol.isClass =>
            s"$paramName = ${initCaseClass(t, indent + "  ")}"
          case _ =>
            s"$paramName = ${defaultValueForType(paramType)}"
        }
      }

      s"${t.typeSymbol.name}(\n$indent  ${paramStrings.mkString(s",\n$indent  ")}\n$indent)"
    }

    initCaseClass(typeOf[T])
  }
}
