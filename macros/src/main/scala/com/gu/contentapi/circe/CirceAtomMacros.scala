package com.gu.contentapi.circe

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object CirceAtomMacros {
  def getAtomTypeFieldName[B](arg: B): Option[String] = macro CirceAtomMacrosImpl.getAtomTypeFieldName[B]
}

class CirceAtomMacrosImpl(val c: blackbox.Context) {

  import c.universe._
  def getAtomTypeFieldName[B : WeakTypeTag](arg: c.Expr[B]) = {
    val baseType = weakTypeOf[B].typeSymbol
    if(!(baseType.isClass && baseType.asClass.isSealed)) {
      c.abort(c.enclosingPosition, "Not a valid AtomType base class: expected a sealed class")
    }

    val patterns = baseType.asClass.knownDirectSubclasses.map { cl =>
      val nm = cl.name
      val res = if(nm == TypeName("EnumUnknownAtomType")) q"None" else q"""Some(${nm.toString.toLowerCase})"""
      cq"""_: $cl => $res"""
    }

    q"""$arg match {
      case ..$patterns
      case _ => None
    }"""
  }

}
