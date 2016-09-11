package com.gu.contentapi.json

import com.gu.contentapi.client.model.v1.VideoStatsResponse
import com.gu.contentapi.json.utils.JsonHelpers._
import com.gu.contentapi.json.CirceSerialization._
import org.scalatest.{FlatSpec, Matchers}

class JsonParserVideoStatsTest  extends FlatSpec with Matchers {

  val videoStatsResponse = parseJson[VideoStatsResponse](loadJson("video-stats.json"))

  "video stats parser" should "parse basic response fields" in {
    videoStatsResponse.status should be ("ok")
  }

  it should "parse the mostViewedVideos" in {
    videoStatsResponse.mostViewedVideos.size should be (3)
    val video = videoStatsResponse.mostViewedVideos.head
    video.id should be ("gu-video-5706597fe4b0c18a88913e06")
    video.count should be (98)
  }

}
