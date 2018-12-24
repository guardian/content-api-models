namespace * contentatom.commonsdivision
namespace java com.gu.contentatom.thrift.atom.commonsdivision
#@namespace scala com.gu.contentatom.thrift.atom.commonsdivision

include "../shared.thrift"

struct MP {
  1: required string name
  2: required string party
}

struct Votes {
  1: required list<MP> ayes
  2: required list<MP> noes
}

struct CommonsDivision {
  1: required string parliamentId
  2: optional string description
  3: required shared.DateTime date
  4: required Votes votes
}
