package com.gu.contentapi.utils

import com.gu.contentapi.client.model.v1._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

object CapiModelEnrichment {

  implicit class RichCapiDateTime(val cdt: CapiDateTime) extends AnyVal {
    def toJodaDateTime: DateTime = new DateTime(cdt.iso8601)
  }

  implicit class RichJodaDateTime(val dt: DateTime) extends AnyVal {
    def toCapiDateTime: CapiDateTime = CapiDateTime.apply(dt.getMillis, dt.toString(ISODateTimeFormat.dateTime()))
  }

}
