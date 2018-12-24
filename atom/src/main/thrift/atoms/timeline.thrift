namespace * contentatom.timeline
namespace java com.gu.contentatom.thrift.atom.timeline
#@namespace scala com.gu.contentatom.thrift.atom.timeline

include "entity.thrift"
include "../shared.thrift"

struct TimelineItem {
  1: required string title
  2: required shared.DateTime date
  3: optional string body
  4: optional list<entity.Entity> entities
  5: optional string dateFormat
  6: optional shared.DateTime toDate
}

struct TimelineAtom {
  1: optional string typeLabel
  3: required list<TimelineItem> events
  4: optional string description
}
