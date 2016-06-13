package com.gu.contentapi.json

import com.gu.contentapi.client.model.v1.{Atoms, Edition, _}
import com.gu.contentatom.thrift.Atom
import org.json4s._

object JsonDeserializer {

  implicit val formats: Formats = Serialization.formats

  def deserializeSection(jvalue: JValue): Option[Section] = jvalue.extractOpt[Section]

  def deserializeTag(jvalue: JValue): Option[Tag] = jvalue.extractOpt[Tag]

  def deserializeNetworkFront(jvalue: JValue): Option[NetworkFront] = jvalue.extractOpt[NetworkFront]

  def deserializePackage(jvalue: JValue): Option[Package] = jvalue.extractOpt[Package]

  def deserializeContent(jvalue: JValue): Option[Content] = jvalue.extractOpt[Content]

  def deserializeEdition(jvalue: JValue): Option[Edition] = jvalue.extractOpt[Edition]

  def deserializeAtom(jvalue: JValue): Option[Atom] = jvalue.extractOpt[Atom]

  def deserializeAtoms(jvalue: JValue): Option[Atoms] = jvalue.extractOpt[Atoms]

}
