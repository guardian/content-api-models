package com.gu.contentapi.scala

import org.scalatest.{FlatSpec, Matchers}
import org.apache.thrift.transport.TMemoryBuffer
import org.apache.thrift.protocol.TCompactProtocol
import com.gu.contentapi.client.model.v1.Content
import com.gu.contentapi.client.model.v1.ContentType
import com.gu.contentapi.client.model.v1.CapiDateTime
import org.apache.thrift.transport._
import java.nio.file.Files
import java.nio.file.Path
import com.twitter.scrooge.ThriftStructCodec
import org.scalatest.Assertion
import com.twitter.scrooge.ThriftStruct

import com.gu.contentapi.client.model.v1.{ItemResponse, ProductSummaryElementFields, SearchResponse, SummaryProductRef, ProductSummaryDisplayType}

class ThriftRoundTripSpec extends FlatSpec with Matchers {
  it should "round-trip an ItemResponse" in {
    checkRoundTrip(
      Path.of("hot-divorcee-summer.binary.thrift"),
      ItemResponse,
      (item: ItemResponse) => {
        item.content.get.fields.get.headline shouldBe Some("Hot divorcee summer: get ready for big hats, hot sex and don’t-care energy")
      }
    )
  }

  it should "round-trip a SearchResponse" in {
    checkRoundTrip(
      Path.of("search-ballincollig.binary.thrift"),
      SearchResponse,
      (response: SearchResponse) => {
        response.results(0).fields.get.headline shouldBe Some("Christy review – outstanding actors and Cork landmarks shine in a moving and funny Irish drama")
      }
    )
  }

  it should "round-trip a ProductSummaryElementFields" in {
    checkRoundTrip(Path.of("product-summary-element-fields.binary.thrift"), ProductSummaryElementFields)
  }

  def checkRoundTrip[T <: ThriftStruct](
    resourcePath: Path,
    codec: ThriftStructCodec[T],
    assertion: T => Unit = (t: T) => ()
  ) = {
    val resourcesPath = Path.of("scala", "src", "test", "resources")
    val inputBytes: Array[Byte] = Files.readAllBytes(resourcesPath.resolve(resourcePath))
    val transport = new TMemoryBuffer(inputBytes.length)
    transport.write(inputBytes)
    val protocol = new TCompactProtocol(transport)
    val struct = codec.decode(protocol)
    val outputTransport = new TMemoryBuffer(inputBytes.length)
    val outputProtocol = new TCompactProtocol(outputTransport)
    struct.write(outputProtocol)
    outputTransport.getArray() shouldEqual inputBytes
    assertion(struct)
  }
}
