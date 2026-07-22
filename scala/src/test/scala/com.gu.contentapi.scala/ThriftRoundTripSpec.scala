package com.gu.contentapi.scala

import org.apache.thrift.transport._
import org.apache.thrift.protocol.{TProtocol, TBinaryProtocol, TCompactProtocol}
import com.gu.contentapi.client.model.v1.{
  Content,
  ContentType,
  CapiDateTime,
  ItemResponse,
  ProductSummaryElementFields,
  SearchResponse,
  SummaryProductRef,
  ProductSummaryDisplayType,
  Tag,
  TagType,
}
import com.gu.contentatom.thrift.AtomType
import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}
import java.nio.file.{Files, Path}

import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ThriftRoundTripSpec extends AnyFlatSpec with Matchers {
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

  it should "round-trip an atom" in {
    compactToBinary(
      Path.of("atom-cta-2bcdfd12-5e96-493c-8b18-a8d4c53df938.binary.thrift"),
      ItemResponse
    )
    checkRoundTrip(
      Path.of("atom-cta-2bcdfd12-5e96-493c-8b18-a8d4c53df938.binary.thrift"),
      ItemResponse,
      (item: ItemResponse) => {
        item.cta.map(_.atomType) shouldBe Some(AtomType.Cta)
      }
    )
  }

  it should "round-trip a ProductSummaryElementFields" in {
    checkRoundTrip(Path.of("product-summary-element-fields.binary.thrift"), ProductSummaryElementFields)
  }

  it should "round-trip TagType enum values" in {
    for {
      (rawPath, tagType) <- Seq(
        "some-contributor-tag.binary.thrift" -> TagType.Contributor,
        "some-keyword-tag.binary.thrift" -> TagType.Keyword,
        "some-series-tag.binary.thrift" -> TagType.Series,
        "some-newspaper-book-section-tag.binary.thrift" -> TagType.NewspaperBookSection,
        "some-newspaper-book-tag.binary.thrift" -> TagType.NewspaperBook,
        "some-blog-tag.binary.thrift" -> TagType.Blog,
        "some-tone-tag.binary.thrift" -> TagType.Tone,
        "some-type-tag.binary.thrift" -> TagType.Type,
        "some-publication-tag.binary.thrift" -> TagType.Publication,
        "some-tracking-tag.binary.thrift" -> TagType.Tracking,
        "some-paid-content-tag.binary.thrift" -> TagType.PaidContent,
        "some-campaign-tag.binary.thrift" -> TagType.Campaign,
      )
    } yield {
      checkRoundTrip(Path.of(rawPath), Tag, (tag: Tag) => tag.`type` shouldBe tagType)
    }
  }

  def checkRoundTrip[T <: ThriftStruct](
    resourcePath: Path,
    codec: ThriftStructCodec[T],
    assertion: T => Unit = (t: T) => ()
  ) = {
    for {
      protocol <- Seq(Compact, Binary)
    } yield {
      val (inputBytes, struct) = readProtocol(protocol, resourcePath, codec)
      val outputTransport = new TMemoryBuffer(inputBytes.length)
      val outputProtocol = protocol(outputTransport)
      struct.write(outputProtocol)
      outputTransport.getArray() shouldEqual inputBytes
      assertion(struct)
    }
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
    for {
      protocol <- Seq(Compact, Binary)
    } yield {
      writeProtocol(protocol, resourcePath, value)
    }
  }

  def compactToBinary[T <: ThriftStruct](
    resourcePath: Path,
    codec: ThriftStructCodec[T],
  ) = {
    val (inputBytes, struct) = readProtocol(Compact, resourcePath, codec)
    writeProtocol(Binary, resourcePath, struct)
  }

  def readProtocol[T <: ThriftStruct](
    protocol: Protocol,
    resourcePath: Path,
    codec: ThriftStructCodec[T],
  ): (Array[Byte], T) = {
    val resourcesPath = Path.of("scala", "src", "test", "resources", protocol.name)
    val inputBytes: Array[Byte] = Files.readAllBytes(resourcesPath.resolve(resourcePath))
    val transport = new TMemoryBuffer(inputBytes.length)
    transport.write(inputBytes)
    val inputProtocol = protocol(transport)
    return (inputBytes, codec.decode(inputProtocol))
  }

  def writeProtocol[T <: ThriftStruct](
    protocol: Protocol,
    resourcePath: Path,
    value: T
  ): Unit = {
    val resourcesPath = Path.of("scala", "src", "test", "resources", protocol.name)
    val outputStream = Files.newOutputStream(resourcesPath.resolve(resourcePath))
    val transport = new TIOStreamTransport(outputStream)
    val outputProtocol = protocol(transport)
    value.write(outputProtocol)
  }

  sealed trait Protocol {
    val name: String
    def apply(transport: TTransport): TProtocol
  }
  case object Compact extends Protocol {
    val name = "TCompactProtocol"
    def apply(transport: TTransport) = new TCompactProtocol(transport)
  }
  case object Binary extends Protocol {
    val name = "TBinaryProtocol"
    def apply(transport: TTransport) = new TBinaryProtocol(transport)
  }
}
