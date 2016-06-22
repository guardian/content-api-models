package com.gu.contentapi.circe

import scala.language.experimental.macros
import scala.reflect.macros.{blackbox,whitebox}
import shapeless.LabelledGeneric
import com.twitter.scrooge.{ThriftEnum, ThriftStruct}
import io.circe.Decoder

/**
  * Macros for Circe deserialization of various Scrooge-generated classes.
  */
object CirceScroogeHelper {

  /**
    * Macro to help Circe find LabelledGeneric instances for Scrooge Thrift structs.
    * The macro is needed because Scrooge generates non-sealed traits.
    *
    * Scrooge represents
    *
    * {{{
    * struct Foo {
    *   1: string a
    *   2: i32 b
    * }
    * }}}
    *
    * As a trait plus a companion object with a nested `Immutable` class.
    * Roughly:
    *
    * {{{
    * object Foo {
    *   def apply(a: String, b: Int) = new Immutable(a, b)
    *   class Immutable(val a: String, val b: Int) extends Foo
    * }
    *
    * trait Foo {
    *   def a: String
    *   def b: Int
    * }
    * }}}
    *
    * The code to deal with this situation was copy-pasted from
    * https://github.com/travisbrown/scrooge-circe-demo
    */
  implicit def materializeStructGen[A <: ThriftStruct]: LabelledGeneric[A] = macro materializeStructGen_impl[A]

  def materializeStructGen_impl[A: c.WeakTypeTag](c: whitebox.Context): c.Tree = {
    import c.universe._

    val A = weakTypeOf[A]
    val I = A.companion.member(TypeName("Immutable")) match {
      case NoSymbol => c.abort(c.enclosingPosition, "Not a valid Scrooge class")
      case symbol => symbol.asType.toType
    }
    val N = appliedType(typeOf[NoPassthrough[_, _]].typeConstructor, A, I)

    q"""{
      val np = _root_.shapeless.the[$N]
      new _root_.shapeless.LabelledGeneric[$A] {
        type Repr = np.Without
        def to(t: $A): Repr = np.to(t.copy().asInstanceOf[$I])
        def from(r: Repr): $A = np.from(r)
      }
    }"""
  }

  /**
    * Macro to provide custom decoding of Thrift enums
    */
  implicit def decodeThriftEnum[A <: ThriftEnum]: Decoder[A] = macro decodeThriftEnum_impl[A]

  def decodeThriftEnum_impl[A: c.WeakTypeTag](c: blackbox.Context): c.Tree = {
    import c.universe._

    val A = weakTypeOf[A]
    val typeName = A.typeSymbol.name.toString
    val valueOf = A.companion.member(TermName("valueOf"))
    val unknown = A.companion.member(TermName(s"EnumUnknown$typeName"))

    q"""
    _root_.io.circe.Decoder.instance((cursor: _root_.io.circe.HCursor) => {
      cursor.focus.asString match {
        case _root_.scala.Some(value) =>
          val withoutHyphens = value.replaceAllLiterally("-", "")
          _root_.cats.data.Xor.right($valueOf(withoutHyphens).getOrElse($unknown.apply(-1)))
        case _ =>
          _root_.cats.data.Xor.left(_root_.io.circe.DecodingFailure($typeName, cursor.history))
      }
    })
    """
  }

}
