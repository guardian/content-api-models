package benchmark

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

import java.nio.charset.StandardCharsets
import com.google.common.io.Resources

import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.json.CirceDecoders._
import com.gu.contentapi.json.CirceEncoders._
import io.circe.syntax._

import io.circe.Json

case class SearchResp(
      status: String,
      userTier: String,
      total: Int,
      startIndex: Int,
      pageSize: Int,
      currentPage: Int,
      pages: Int,
      orderBy: String,
      results: List[Json]
)
case class WrappedLegacyResponse(response: SearchResp)

@State(Scope.Thread)
class JsonDecodeBenchmark {

  def loadJson(filename: String): Json = {
    val rawJson = Resources.toString(Resources.getResource(filename), StandardCharsets.UTF_8)
    val circeJson = {
      import io.circe.jawn._
      parse(rawJson).getOrElse(Json.Null)
    }
    circeJson
  }

  val contentJson = loadJson("content-with-blocks.json")
  val massiveContentsListJson = loadJson("massive-contents-list.json")
  val contentOpt = contentJson.as[Content].leftMap(e => throw e).toOption

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def circeDecode(bh: Blackhole) = {
    bh.consume(contentJson.as[Content])
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def circeEncode(bh: Blackhole) = {
    contentOpt.foreach(c => bh.consume(c.asJson))
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def circeDecodeEachContent(bh: Blackhole) = {
    val results =
      for {
        i <- 1 to 10
        content <- contentJson.as[Content].leftMap(e => throw e).toOption
      } yield content
    val response = SearchResponse(
      status = "ok",
      userTier = "foo",
      total = 123,
      startIndex = 123,
      pageSize = 123,
      currentPage = 123,
      pages = 123,
      orderBy = "foo",
      results = results
    )
    bh.consume(response)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def circeDecodeEachMassiveContent(bh: Blackhole) = {
    massiveContentsListJson.asArray.foreach { contentJsonArray: List[Json] =>
      val results =
        for {
          contentJson <- contentJsonArray
          content <- contentJson.as[Content].leftMap(e => throw e).toOption
        } yield content
      val response = SearchResponse(
        status = "ok",
        userTier = "foo",
        total = 123,
        startIndex = 123,
        pageSize = 123,
        currentPage = 123,
        pages = 123,
        orderBy = "foo",
        results = results
      )
      bh.consume(response)
    }
  }

}
