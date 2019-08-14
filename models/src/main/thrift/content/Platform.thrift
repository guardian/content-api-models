namespace scala com.gu.contentapi.client.model.v1

include "CapiDateTime.thrift"
include "Tag.thrift"

union PublicationInstance {
  1: WebFields web

  2: PrintFields print

  3: EditionFields edition
}

struct WebFields {
  0: required string url // isn't this determined by the path???

  1: required string title
}

struct PrintFields {
  0: required CapiDateTime.CapiDateTime issueDate

  // These three tags might be better modeled as their own special thing but
  // cheat for now and re-use the existing tags

  // This should be the publication tag
  1: required Tag.Tag publication

  // This should be the book tag
  2: required Tag.Tag book

  // This should be the book section tag
  3: required Tag.Tag bookSection

  // The page number this article was found on
  // Interestingly this might be multiple numbers for a spread and might change
  //   from one edition (in the print sense) to another
  // Not sure what the current logic is but worth investigating...
  4: required i32 pageNumber
}

struct EditionFields {
  0: required CapiDateTime.CapiDateTime issueDate
}