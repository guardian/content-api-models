package com.gu.contentapi.json.utils

import java.nio.charset.StandardCharsets

import com.google.common.io.Resources

object JsonLoader {
  def loadJson(filename: String): String = {
    Resources.toString(Resources.getResource(s"templates/$filename"), StandardCharsets.UTF_8)
  }
}