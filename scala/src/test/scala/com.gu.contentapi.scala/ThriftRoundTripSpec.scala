package com.gu.contentapi.scala

import org.scalatest.{FlatSpec, Matchers}
import com.gu.contentapi.client.model.v1.SearchResponse
import org.apache.thrift.transport.TMemoryBuffer
import org.apache.thrift.protocol.TCompactProtocol
import com.gu.contentapi.client.model.v1.Content
import com.gu.contentapi.client.model.v1.ContentType
import com.gu.contentapi.client.model.v1.CapiDateTime
import org.apache.thrift.transport._
import java.nio.file.Files
import java.nio.file.Path
import com.gu.contentapi.client.model.v1.ItemResponse
import com.twitter.scrooge.ThriftStructCodec
import org.scalatest.Assertion
import com.twitter.scrooge.ThriftStruct

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

  /**
   * Helper for generating test files for a type.
   *
   * Start with a test like this:
   *
   * {{{
   * it should "round-trip a ProductSummaryElementFields" in {
   *   val fields = ProductSummaryElementFields(Some("Something"),ProductSummaryDisplayType.Carousel,Some(List(SummaryProductRef(Some("product-id"),Some(0)))),Some("An id"))
   *   val testPath = Path.of("productSummaryElementFields.binary.thrift")
   *   thriftToFile(testPath, fields)
   *   checkRoundTrip(testPath, ProductSummaryElementFields)
   * }
   * }}}
   *
   * Running `sbt test` will generate an appropriate resource file by writing
   * out the example value (`fields` here), and then you can remove the
   * `thriftToFile` call and simplify the test.
   */
  def thriftToFile[T <: ThriftStruct](
    resourcePath: Path,
    value: T,
  ) = {
    val resourcesPath = Path.of("scala", "src", "test", "resources")
    val outputStream = Files.newOutputStream(resourcesPath.resolve(resourcePath))
    val transport = new TIOStreamTransport(outputStream)
    val protocol = new TCompactProtocol(transport)
    value.write(protocol)
  }
}
