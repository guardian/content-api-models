package com.gu.contentapi.json

import com.gu.contentapi.json.utils.JsonLoader
import com.gu.contentatom.thrift.Atom
import com.gu.contentapi.json.CirceSerialization._
import com.gu.contentapi.circe.CirceScroogeMacros._
import io.circe.generic.auto._
import io.circe._
import io.circe.parser._
import org.scalatest.{FlatSpec, Matchers}
import org.json4s.jackson.JsonMethods

class CirceSpec extends FlatSpec with Matchers {

  it should "deserialize an Atom using circe" in {
    def deserialize(): String = {
      val jsonString = JsonLoader.loadJson("quiz.json")
      val json: Json = parse(jsonString).getOrElse(Json.Null)
      val thrift = json.as[Atom](atomDecoder)
      thrift.getOrElse(Json.Null).toString
    }
    time(deserialize)
  }

  it should "deserialize an Atom using json4s" in {
    def deserialize(): String = {
      implicit val formats = Serialization.formats
      import org.json4s._

      val jsonString = JsonLoader.loadJson("quiz.json")
      val json: JValue = JsonMethods.parse(jsonString)
      val thrift = json.extract[Atom]
      thrift.toString
    }
    time(deserialize)
  }

  def time(f: () => String): Unit = {

    for (i <- 0 until 10000) {
      f()
    }

    val t0 = System.nanoTime()
    for (i <- 0 until 100) {
      f()
    }
    val res = f()
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    println(res)
  }
}

