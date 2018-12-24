namespace * contentatom.qanda
namespace java com.gu.contentatom.thrift.atom.qanda
#@namespace scala com.gu.contentatom.thrift.atom.qanda

include "../shared.thrift"
include "storyquestions.thrift"

struct QAndAItem {
  1: optional string title
  2: required string body
}

struct QAndAAtom {
  1: optional string typeLabel
  3: optional shared.Image eventImage
  4: required QAndAItem item
  5: optional storyquestions.Question question
  // in the future, we may add an optional attribute for
  // journalists to provide some aside
}
