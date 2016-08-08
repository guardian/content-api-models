package com.gu.contentapi.json

import com.gu.contentapi.json.utils.JsonLoader
import com.gu.contentatom.thrift.Atom
import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.json.CirceSerialization._

import io.circe._
import io.circe.parser._

import org.json4s.jackson.JsonMethods

import cats.data.Xor.Right

import org.scalatest.{FlatSpec, Matchers}

class CirceSpec extends FlatSpec with Matchers {

  def compareAgainstJson4s[T: Decoder: Manifest](filename: String) = {
    val jsonString = JsonLoader.loadJson(filename)

    val usingCirce = {
      val json: Json = parse(jsonString).getOrElse(Json.Null)
      json.as[T]
    }

    val usingJson4s = {
      import org.json4s._
      implicit val formats = Serialization.formats + new CustomSerializer[Boolean](format => (
        {
          case JBool(value) => value
          case JString("true") => true
          case JString("false") => false
        },
        {
          case true => JBool(true)
          case false => JBool(false)
        }
        ))
      val json: JValue = JsonMethods.parse(jsonString)
      json.extract[T]
    }

    usingCirce should be(Right(usingJson4s))
  }

  it should "be able to deserialize a Json number to a String" in {
    parse(""" "123456789" """).getOrElse(Json.Null).as[String] should be(Right("123456789"))
    parse(""" 123456789 """).getOrElse(Json.Null).as[String] should be(Right("123456789"))
    parse(""" 123456789 """).getOrElse(Json.Null).as[Long] should be(Right(123456789L))
  }

  it should "deserialize a CapiDateTime using circe" in {
    val json: Json = parse(""" "2016-05-01T01:23:45Z" """).getOrElse(Json.Null)
    val thrift = json.as[CapiDateTime]
    thrift should be(Right(CapiDateTime(1462065825000L, "2016-05-01T01:23:45.000Z")))
  }

  it should "deserialize an Atom with the same result as json4s" in {
    compareAgainstJson4s[Atom]("quiz.json")
  }

  it should "deserialize a Tag with the same result as json4s" in {
    compareAgainstJson4s[Tag]("tag.json")
  }

  it should "deserialize a Section with the same result as json4s" in {
    compareAgainstJson4s[Section]("section.json")
  }

  it should "deserialize a Content with the same result as json4s" in {
    compareAgainstJson4s[Content]("content.json")
  }

  it should "deserialize a Content (including a crossword) with the same result as json4s" in {
    compareAgainstJson4s[Content]("content-with-crossword.json")
  }

  it should "deserialize a Content (including a media atom) with the same result as json4s" in {
    compareAgainstJson4s[Content]("content-with-atom-media.json")
  }

  it should "deserialize a Content (including an explainer atom) with the same result as json4s" in {
    compareAgainstJson4s[Content]("content-with-atom-explainer.json")
  }

  it should "deserialize a Content (including blocks) with the same result as json4s" in {
    compareAgainstJson4s[Content]("content-with-blocks.json")
  }

}

