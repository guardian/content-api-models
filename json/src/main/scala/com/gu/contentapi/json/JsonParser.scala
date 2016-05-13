package com.gu.contentapi.json

import com.gu.contentapi.client.model.v1._

import org.json4s.Formats
import org.json4s.jackson.JsonMethods

object JsonParser {
  import Serialization.destringifyFields

  implicit val formats: Formats = Serialization.formats

  def parseItem(json: String): ItemResponse = {
    (JsonMethods.parse(json) \ "response").transformField(destringifyFields).extract[ItemResponse]
  }

  def parseSearch(json: String): SearchResponse = {
    (JsonMethods.parse(json) \ "response").transformField(destringifyFields).extract[SearchResponse]
  }

  def parseRemovedContent(json: String): RemovedContentResponse = {
    (JsonMethods.parse(json) \ "response").extract[RemovedContentResponse]
  }

  def parseTags(json: String): TagsResponse = {
    (JsonMethods.parse(json) \ "response").extract[TagsResponse]
  }

  def parseSections(json: String): SectionsResponse = {
    (JsonMethods.parse(json) \ "response").extract[SectionsResponse]
  }

  def parseEditions(json: String): EditionsResponse = {
    (JsonMethods.parse(json) \ "response").extract[EditionsResponse]
  }

  def parseVideoStats(json: String): VideoStatsResponse = {
    (JsonMethods.parse(json) \ "response").extract[VideoStatsResponse]
  }

  def parsePackages(json: String): PackageResponse = {
    (JsonMethods.parse(json) \ "response").extract[PackageResponse]
  }

  def parseError(json: String): Option[ErrorResponse] = for {
    parsedJson <- JsonMethods.parseOpt(json)
    response = parsedJson \ "response"
    errorResponse <- response.extractOpt[ErrorResponse]
  } yield errorResponse

  def parseContent(json: String): Content = {
    JsonMethods.parse(json).transformField(destringifyFields).extract[Content]
  }

}
