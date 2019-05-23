namespace scala com.gu.contentapi.client.model.v1

include "CapiDateTime.thrift"

struct Platforms {
  0: optional WebPlatform web;

  1: optional PrintPlatform print;

  2: optional EditionPlatform edition;
}

struct WebPlatform {
  0: required string url

  1: required string title

  2: required CapiDateTime publicationDate
}

struct PrintPlatform {
  0: required i32 pageNumber
    
  1: required CapiDateTime editionDate
}

struct EditionPlatform {
  0: required CapiDateTime editionDate
}