package com.gu.contentapi.circe

import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import com.twitter.scrooge.{ThriftEnum, ThriftStruct}
import io.circe.Decoder

/**
  * Macros for Circe deserialization of various Scrooge-generated classes.
  */
object CirceScroogeMacros {

  /**
    * Macro to provide custom decoding of Thrift structs using the companion object's `apply` method.
    *
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
    */
  implicit def decodeThriftStruct[A <: ThriftStruct]: Decoder[A] = macro decodeThriftStruct_impl[A]

  def decodeThriftStruct_impl[A: c.WeakTypeTag](c: blackbox.Context): c.Tree = {
    import c.universe._

    val A = weakTypeOf[A]
    val apply = A.companion.member(TermName("apply")) match {
      case symbol if symbol.isMethod && symbol.asMethod.paramLists.size == 1 => symbol.asMethod
      case _ => c.abort(c.enclosingPosition, "Not a valid Scrooge class: could not find the companion object's apply method")
    }
    val params = apply.paramLists.head.zipWithIndex.map { case (param, i) =>
      val name = param.name
      val tpe = param.typeSignature
      val fresh = c.freshName(name)

      // Note the use of shapeless `Lazy` to work around a problem with diverging implicits.
      // If you try to summon an implicit for heavily nested type, e.g. `Decoder[Option[Seq[String]]]` then the compiler sometimes gives up.
      // Wrapping with `Lazy` fixes this issue.
      val decodeParam = q"""cursor.get[$tpe](${name.toString})(_root_.scala.Predef.implicitly[_root_.shapeless.Lazy[_root_.io.circe.Decoder[$tpe]]].value)"""

      val expr =
        if (param.asTerm.isParamWithDefault) {
          // Fallback to param's default value if the JSON field is not present (or can't be decoded for some other reason).
          // Note: reverse-engineering the name of the default value because it's not easily available in the reflection API.
          val defaultValue = A.companion.member(TermName("apply$default$" + (i + 1)))
          fq"""$fresh <- $decodeParam.orElse(_root_.cats.data.Xor.right($defaultValue))"""
        } else
          fq"""$fresh <- $decodeParam"""
      (fresh, expr)
    }

    /*
      Generates code that looks like this:

      val f$macro$123 = { (cursor: HCursor) =>
        for {
          id$macro$456 <- cursor.get[String]("id")
          type$macro$789 <- cursor.get[TagType]("type")
          ...
        } yield Tag.apply(id$macro$456, type$macro$789)
      }

      Decoder.instance(apply$macro$123)

     */
    val tree = q"""{
      val f = (cursor: _root_.io.circe.HCursor) => for (..${params.map(_._2)}) yield $apply(..${params.map(_._1)})
      _root_.io.circe.Decoder.instance(f)
    }"""
    //println(showCode(tree))
    tree
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
