package org.julianfrech.samples

import java.sql.Timestamp
import scala.reflect.ClassTag

trait DefaultValue[T] {
    def value: T
  }

  object DefaultValue {
    implicit val stringDefault: DefaultValue[String] = new DefaultValue[String] {
      val value = "\"\""
    }
    implicit val intDefault: DefaultValue[Int] = new DefaultValue[Int] {
      val value = 0
    }
    implicit val doubleDefault: DefaultValue[Double] = new DefaultValue[Double] {
      val value = 0.0
    }
    implicit val floatDefault: DefaultValue[Float] = new DefaultValue[Float] {
      val value = 0.0f
    }
    implicit val longDefault: DefaultValue[Long] = new DefaultValue[Long] {
      val value = 0L
    }
    implicit val booleanDefault: DefaultValue[Boolean] = new DefaultValue[Boolean] {
      val value = false
    }
    implicit val charDefault: DefaultValue[Char] = new DefaultValue[Char] {
      val value = '\u0000'
    }
    implicit val byteDefault: DefaultValue[Byte] = new DefaultValue[Byte] {
      val value = 0.toByte
    }
    implicit val shortDefault: DefaultValue[Short] = new DefaultValue[Short] {
      val value = 0.toShort
    }
    implicit val unitDefault: DefaultValue[Unit] = new DefaultValue[Unit] {
      val value = ()
    }
    implicit val timestampDefault: DefaultValue[Timestamp] = new DefaultValue[Timestamp] {
      val value = new Timestamp(System.currentTimeMillis())
    }
    implicit def arrayDefault[T: ClassTag]: DefaultValue[Array[T]] =
      new DefaultValue[Array[T]] {
        val value = Array.empty[T]
      }


    implicit def optionDefault[T: DefaultValue]: DefaultValue[Option[T]] =
      new DefaultValue[Option[T]] {
        val value = Some(implicitly[DefaultValue[T]].value)
      }

    implicit def listDefault[T: DefaultValue]: DefaultValue[List[T]] =
      new DefaultValue[List[T]] {
        val value = List(implicitly[DefaultValue[T]].value)
      }

    implicit def vectorDefault[T: DefaultValue]: DefaultValue[Vector[T]] =
      new DefaultValue[Vector[T]] {
        val value = Vector(implicitly[DefaultValue[T]].value)
      }

    implicit def seqDefault[T: DefaultValue]: DefaultValue[Seq[T]] =
      new DefaultValue[Seq[T]] {
        val value = Seq(implicitly[DefaultValue[T]].value)
      }

    implicit def setDefault[T: DefaultValue]: DefaultValue[Set[T]] =
      new DefaultValue[Set[T]] {
        val value = Set(implicitly[DefaultValue[T]].value)
      }

    implicit def mapDefault[K: DefaultValue, V: DefaultValue]: DefaultValue[Map[K, V]] =
      new DefaultValue[Map[K, V]] {
        val value = Map(implicitly[DefaultValue[K]].value -> implicitly[DefaultValue[V]].value)
      }

    def createDefaultValue[T](defaultValue: T): DefaultValue[T] = new DefaultValue[T] {
      val value = defaultValue
    }

    def apply[T](implicit default: DefaultValue[T]): DefaultValue[T] = default

  }