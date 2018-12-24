namespace * contentatom.profile
namespace java com.gu.contentatom.thrift.atom.profile
#@namespace scala com.gu.contentatom.thrift.atom.profile

include "entity.thrift"
include "../shared.thrift"

struct ProfileItem {
  1: optional string title
  2: required string body
}

struct ProfileAtom {
  1: optional string typeLabel
  3: optional shared.Image headshot
  4: required list<ProfileItem> items
  5: optional entity.Entity entity
}
