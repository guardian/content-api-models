package com.gu.contentapi.json.circe

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context
import shapeless.LabelledGeneric
import com.twitter.scrooge.ThriftStruct

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
object CirceScroogeHelper {

  implicit def materializeStructGen[A <: ThriftStruct]: LabelledGeneric[A] = macro materializeStructGen_impl[A]

  def materializeStructGen_impl[A: c.WeakTypeTag](c: Context): c.Tree = {
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

}
