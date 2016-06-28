package benchmark

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

import java.nio.charset.StandardCharsets
import com.google.common.io.Resources

import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.json._

import org.json4s.JValue
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
      results: List[JValue]
)
case class WrappedLegacyResponse(response: SearchResp)

@State(Scope.Thread)
class JsonDecodeBenchmark {

  def loadJson(filename: String): (JValue, Json) = {
    val rawJson = Resources.toString(Resources.getResource(filename), StandardCharsets.UTF_8)
    val jvalue = org.json4s.jackson.JsonMethods.parse(rawJson)
    val circeJson = {
      import io.circe.jawn._
      parse(rawJson).getOrElse(Json.Null)
    }
    (jvalue, circeJson)
  }

  val (contentJvalue: JValue, contentJson: Json) = loadJson("content-with-blocks.json")
  val (searchResponseJvalue, searchResponseJson) = loadJson("search-response.json")

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def json4s(bh: Blackhole) = {
    bh.consume(JsonDeserializer.deserializeContent(contentJvalue))
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def circe(bh: Blackhole) = {
    import com.gu.contentapi.json.CirceSerialization._
    bh.consume(contentJson.as[Content])
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def json4sThenCirce(bh: Blackhole) = {
    bh.consume(CirceJsonDeserializer.deserializeContent(contentJvalue))
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def json4sDeserializeWholeResponse(bh: Blackhole) = {
    implicit val formats = JsonDeserializer.formats
    val resp = WrappedLegacyResponse(SearchResp(
      status = "ok",
      userTier = "foo",
      total = 123,
      startIndex = 123,
      pageSize = 123,
      currentPage = 123,
      pages = 123,
      orderBy = "foo",
      results = List.fill(10)(contentJvalue)
    ))
    val jsonString = org.json4s.jackson.Serialization.write(resp)
    bh.consume(JsonParser.parseSearch(jsonString))
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def circeDeserializeEachContent(bh: Blackhole) = {
    val results = 
      for {
        i <- 1 to 10
        content <- CirceJsonDeserializer.deserializeContent(contentJvalue).toOption
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
