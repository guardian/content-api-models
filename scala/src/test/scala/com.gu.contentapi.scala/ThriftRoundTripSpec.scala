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
import com.gu.contentatom.thrift.{AtomData, AtomType}
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

  it should "round-trip atoms" in {
    checkRoundTrip(
      Path.of("atom-cta-2bcdfd12-5e96-493c-8b18-a8d4c53df938.binary.thrift"),
      ItemResponse,
      (item: ItemResponse) => {
        item.cta.map(_.data).collect {
          case AtomData.Cta(cta) => cta
        }.flatMap(_.label) shouldBe Some("Explore Amazon Freight for your shipments")
      }
    )
    checkRoundTrip(
      Path.of("atom-quiz-ed563bff-19cf-49f6-a5c3-a458559f432d.binary.thrift"),
      ItemResponse,
      (item: ItemResponse) => item.quiz.map(_.data).collect {
        case AtomData.Quiz(quiz) => quiz.content.questions(0).questionText
      } shouldBe Some("Paul, 7, asks: who invented the game ‘rock, paper, scissors’?\n")
    )
    checkRoundTrip(
      Path.of("atom-guide-9c862998-6f26-42f1-9243-fcc5766486cf.binary.thrift"),
      ItemResponse,
      (item: ItemResponse) => item.guide.map(_.data).collect {
        case AtomData.Guide(guide) => guide.items(0).body
      } shouldBe Some("<p><b>Sandown </b>1.55 Arry Up 2.25 Jinman 2.55 Aperoll 3.30 Kamaway 4.07 Probation 4.42 Albertini Star 5.17 Sail On Sailor</p><p><b>Doncaster </b>2.10 Night Star 2.40 Round The Table 3.10 Ten Clarets 3.45 Sargent Dennis (nap) 4.15 Jenni 4.48 Palmarian 5.23 Brighlee</p><p><b>Southwell </b>4.53 Finn Ironside 5.28 Hulk Power 6.01 Dovecote 6.36 Beresford Gap 7.11 Little Mester 7.46 Koko Blue 8.21 Hansteen 8.56 My Mate Mackley</p><p><b>Yarmouth </b>5.04 Anchiano 5.39 Panelli 6.14 Hardy’s Hero 6.49 Campani 7.24 Maith Mar Or 7.59 Due Date 8.34 Roi De Coeur</p><p><b>Newbury </b>5.55 Duke Of Burgundy 6.30 Art Of Life 7.05 Always Perfect 7.40 The Craftymaster (nb) 8.15 Dancing Tiger 8.48 Port Louis</p>")
    )
    checkRoundTrip(
      Path.of("atom-explainer-4d42b98e-1b9d-4f95-b256-e12acfd39f21.binary.thrift"),
      ItemResponse,
      (item: ItemResponse) => item.explainer.map(_.data).collect{
        case AtomData.Explainer(explainer) => explainer.title
      } shouldBe Some("What is fracking? ")
    )
    checkRoundTrip(
      Path.of("atom-timeline-32b0d5c4-61cc-4306-847e-7f3b33f31e77.binary.thrift"),
      ItemResponse,
      (item: ItemResponse) => item.timeline.map(_.data).collect {
        case AtomData.Timeline(timeline) => timeline.events(0)
      }.flatMap(_.body) shouldBe Some("<p>Gina Rinehart and Pauline Hanson are seen dining together in Thailand, alongside the former Liberal vice-president Teena McQueen</p>")
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
