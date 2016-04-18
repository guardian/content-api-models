package com.gu.contentapi.json

import org.scalatest.{FlatSpec, Matchers}
import com.gu.contentapi.json.utils.JsonLoader.loadJson

class JsonParserPackagesTest extends FlatSpec with Matchers {

  val packagesResponse = JsonParser.parsePackages(loadJson("packages.json"))

  "packages parser" should "parse basic response fields" in {
    packagesResponse.status should be ("ok")
    packagesResponse.userTier should be ("developer")
    packagesResponse.total should be (39841)
    packagesResponse.startIndex should be (1)
    packagesResponse.pageSize should be (10)
    packagesResponse.currentPage should be (1)
    packagesResponse.pages should be (3985)
  }

  it should "parse the packages" in {
    packagesResponse.results.size should be (10)
    packagesResponse.results.head.packageId should be ("a2665327-e426-423b-a923-319a56c76460")
    packagesResponse.results.head.packageName should be ("Tata steel crisis")
  }

}
