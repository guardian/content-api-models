package benchmark

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

import java.nio.charset.StandardCharsets
import com.google.common.io.Resources

import com.gu.contentapi.client.model.v1.Content
import com.gu.contentapi.json.JsonDeserializer

@State(Scope.Thread)
class JsonDecodeBenchmark {

  val rawJson = Resources.toString(Resources.getResource("content-with-blocks.json"), StandardCharsets.UTF_8)

  val jvalue = org.json4s.jackson.JsonMethods.parse(rawJson)

  val json = {
    import io.circe._
    import io.circe.jawn._
    parse(rawJson).getOrElse(Json.Null)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def json4s(bh: Blackhole) = {
    bh.consume(JsonDeserializer.deserializeContent(jvalue))
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def circe(bh: Blackhole) = {
    import com.gu.contentapi.json.CirceSerialization._
    bh.consume(json.as[Content])
  }

}
