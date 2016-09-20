package com.gu.contentapi.json

import com.gu.contentapi.client.model.v1.{TagType, TagsResponse}
import org.scalatest.{FlatSpec, Matchers}
import com.gu.contentapi.json.utils.JsonHelpers._
import com.gu.contentapi.json.CirceDecoders._

class JsonParserTagsTest extends FlatSpec with Matchers {

  val tagsResponse = parseJson[TagsResponse](loadJson("tags.json"))

  "tags parser" should "parse basic response fields" in {
    tagsResponse.status should be ("ok")
    tagsResponse.userTier should be ("developer")
    tagsResponse.total should be (43880)
    tagsResponse.startIndex should be (1)
    tagsResponse.pageSize should be (10)
    tagsResponse.currentPage should be (1)
    tagsResponse.pages should be (4388)
  }

  it should "parse the tags" in {
    tagsResponse.results.size should be (10)
    tagsResponse.results.head.apiUrl should be ("http://content.guardianapis.com/abu-dhabi/abu-dhabi")
    tagsResponse.results.head.id should be ("abu-dhabi/abu-dhabi")
    tagsResponse.results.head.sectionId should be (Some("abu-dhabi"))
    tagsResponse.results.head.sectionName should be (Some("Abu Dhabi"))
    tagsResponse.results.head.`type` should be (TagType.Keyword)
    tagsResponse.results.head.webTitle should be ("Abu Dhabi")
    tagsResponse.results.head.webUrl should be ("http://www.theguardian.com/abu-dhabi/abu-dhabi")
  }

}
