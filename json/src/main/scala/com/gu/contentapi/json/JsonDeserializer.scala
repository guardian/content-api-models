package com.gu.contentapi.json

import cats.data.Xor
import com.gu.contentapi.client.model.v1.{Atoms, Edition, _}
import com.gu.contentatom.thrift.Atom
import io.circe.Json
import io.circe.parser._
import io.circe._
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

object CirceJsonDeserializer {

  import com.gu.contentapi.json.CirceSerialization._
  import io.circe.syntax._

  def deserializeContent(json: String): Xor[Error, Content] = parse(json).flatMap(_.as[Content])
  def deserializeContent(jvalue: JValue): Xor[Error, Content] = jvalue.asJson.as[Content]

  def deserializeTag(json: String): Xor[Error, Tag] = parse(json).flatMap(_.as[Tag])
  def deserializeTag(jvalue: JValue): Xor[Error, Tag] = jvalue.asJson.as[Tag]

  def deserializeSection(json: String): Xor[Error, Section] = parse(json).flatMap(_.as[Section])
  def deserializeSection(jvalue: JValue): Xor[Error, Section] = jvalue.asJson.as[Section]


}
