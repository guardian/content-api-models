namespace java com.gu.contententity.thrift
#@namespace scala com.gu.contententity.thrift

/** date times are reprsented as i64 - epoch millis */
typedef i64 DateTime

struct Price {
  //ISO 4217 currency code
  1: required string currency
  //Value in the minor unit, e.g. pence/cents
  2: required i32 value
}

struct Geolocation {
  1: required double lat
  2: required double lon
}

struct Address {
  1: optional string formattedAddress
  2: optional i16 streetNumber
  3: optional string streetName
  4: optional string neighbourhood
  5: optional string postTown
  6: optional string locality
  7: optional string country
  8: optional string administrativeAreaLevelOne
  9: optional string administrativeAreaLevelTwo
  10: optional string postCode
}
