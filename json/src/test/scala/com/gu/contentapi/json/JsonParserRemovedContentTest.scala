package com.gu.contentapi.json

import com.gu.contentapi.client.model.v1.RemovedContentResponse
import com.gu.contentapi.json.utils.JsonHelpers._
import org.scalatest.{FlatSpec, Matchers}
import com.gu.contentapi.json.CirceDecoders._

class JsonParserRemovedContentTest extends FlatSpec with Matchers {

  val removedResponse = parseJson[RemovedContentResponse](loadJson("removed.json"))

  "JsonParser.parseRemovedContent" should "parse basic response fields" in {
    removedResponse.status should be ("ok")
    removedResponse.userTier should be ("developer")
    removedResponse.total should be (18198)
    removedResponse.startIndex should be (1)
    removedResponse.pageSize should be (10)
    removedResponse.currentPage should be (1)
    removedResponse.pages should be (1820)
    removedResponse.orderBy should be ("newest")
  }

  it should "parse the list of results" in {
    removedResponse.results.size should be (10)
    removedResponse.results.head should be ("lifeandstyle/interactive/2014/mar/30/live-better")
  }

}
