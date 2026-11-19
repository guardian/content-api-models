package com.gu.storypackage.model.v1

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object RoundtripSpec extends Properties("Roundtrip encoding/decoding") with ThriftSerializer {
    property("transport protocol") = forAll { (e: Event) =>
        deserialize(serializeToBytes(e), Event) == e
    }
}