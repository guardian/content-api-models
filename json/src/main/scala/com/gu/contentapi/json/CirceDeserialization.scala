package com.gu.contentapi.json

import com.gu.contentapi.client.model.v1._
import com.twitter.scrooge.ThriftEnum
import io.circe._
import com.gu.contentapi.circe.CirceScroogeMacros._

object CirceDeserialization {

  implicit val networkFrontEncoder: Encoder[NetworkFront] = encodeThriftStruct[NetworkFront]
  implicit val editionsResponseEncoder: Encoder[EditionsResponse] = encodeThriftStruct[EditionsResponse]

  implicit def thriftEnumEncoder[T <: ThriftEnum]: Encoder[T] = Encoder[String].contramap(t => pascalCaseToHyphenated(t.name))

  private val LowerCaseFollowedByUpperCase = """([a-z])([A-Z])""".r

  /**
    * Convert a PascalCase string to a lowercase hyphenated string
    */
  private def pascalCaseToHyphenated(s: String): String =
    LowerCaseFollowedByUpperCase.replaceAllIn(s, m => m.group(1) + "-" + m.group(2)).toLowerCase

}