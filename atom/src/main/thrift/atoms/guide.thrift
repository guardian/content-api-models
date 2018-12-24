namespace * contentatom.guide
namespace java com.gu.contentatom.thrift.atom.guide
#@namespace scala com.gu.contentatom.thrift.atom.guide

include "../shared.thrift"
include "entity.thrift"

struct GuideItem {
  1: optional string title
  2: required string body
  3: optional list<entity.Entity> entities
}

struct GuideAtom {
  1: optional string typeLabel
  3: optional shared.Image guideImage
  4: required list<GuideItem> items
}
